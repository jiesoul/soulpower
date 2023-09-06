(ns admin.events
  (:require [admin.db :refer [default-db MAX-TOASTS MAX-TIMEOUT]]
            [re-frame.core :refer [reg-event-db reg-event-fx inject-cofx reg-fx dispatch]]
            [admin.auth.events]
            [admin.category.events]
            [admin.article.events]
            [admin.util :as util]
            [cljs.reader]
            [reitit.frontend.controllers :as rfc]
            [reitit.frontend.easy :as rfe]))

;;; Effects ;;;

;; Triggering navigation from events.

(reg-fx
 ::navigate!
 (fn [route]
   (apply rfe/push-state route)))

;;;;; events

(reg-event-fx
 :initialize-db
 [(inject-cofx :local-store-user)]
 (fn [{:keys [local-store-user]} _]
   (let [db (assoc default-db :login-user local-store-user)]
   {:db db})))

(reg-event-fx
 :navigate
 (fn [_ [_ & route]]
   {::navigate! route}))

(reg-event-db
 :navigated
 (fn [db [_ new-match]]
   (let [old-match (:current-route db)
         new-path (:path new-match)
         controllers (rfc/apply-controllers (:controllers old-match) new-match)]
     (js/console.log (str "new-path: " new-path))
     (cond-> (assoc db :current-route (assoc new-match :controllers controllers))
       (= "/" new-match) (-> (assoc :login nil)
                             (assoc :user nil))))))


(reg-event-db
 :pop-toast
 (fn [db _]
   (update-in db [:toasts] #(-> % rest vec))))

(defonce do-timer (js/setInterval (dispatch [:pop-toast]) 3000))

(reg-event-db
 :push-toast
 (fn [db [_ t]]
   (let [toasts (:toasts db)
         toasts (if (>= (count toasts) MAX-TOASTS)
                  (vec (rest toasts))
                  toasts)]
     (assoc db :toasts (conj toasts (assoc t
                                           :id (str (random-uuid))
                                           :timeout MAX-TIMEOUT))))))

(defn push-toast 
  [toasts t]
  (let [toasts (if (>= (count toasts) MAX-TOASTS)
                 (vec (rest toasts))
                 toasts)]
    (conj toasts (assoc t 
                        :id (str (random-uuid))
                        :timeout MAX-TIMEOUT))))

(reg-event-db
 :remove-toast
 (fn [db [_ id]]
   (let [toasts (:toasts db)]
     (assoc db :toasts (->> toasts
                            (remove #(= (:id %) id))
                            vec)))))

(reg-event-fx
 :req-failed-message
 (fn [{:keys [db]} [_ {:keys [response]}]]
   (util/clog "resp failed: " response)
   {:db db
    :fx [[:dispatch [:push-toast {:content (:message response)
                                  :type :error}]]]}))

(reg-event-db                                         
 :api-request-error                                   
 (fn [db [_ {:keys [request-type loading]} response]]
   (let [toasts (:toasts db)
         t {:content (:message response)
            :type :error}]
     (-> db
         (assoc :toasts (push-toast toasts t))
         (assoc-in [:errors request-type] (get-in response [:response :errors]))
         (assoc-in [:loading (or loading request-type)] false)))))

;; current edit

(reg-event-db
 :init-current-route-result
 (fn [db [_ data]]
   (assoc-in db [:current-route :result] data)))

(reg-event-db
 :init-current-route-edit
 (fn [db [_ current]]
   (assoc-in db [:current-route :edit] current)))

(reg-event-db
 :clean-current-route-edit
 (fn [db [_ _]]
   (assoc-in db [:current-route :edit] nil)))

;; modals 

(reg-event-db
 :set-modal-backdrop-show?
 (fn [db [_ v]]
   (assoc-in db [:current-route :modal :back] v)))

(reg-event-fx
 :show-modal
 (fn [{:keys [db]} [_ key]]
   {:db (-> db (assoc-in [:current-route :modal key] true))
    :fx [[:dispatch [:set-modal-backdrop-show? true]]]}))

(reg-event-fx
 :close-modal
 (fn [{:keys [db]} [_ key]]
   {:db (assoc-in db [:current-route :modal key] false)
    :fx [[:dispatch [:set-modal-backdrop-show? false]]
         [:dispatch [:clean-current-route-edit]]]}))

(reg-event-db
 :modal 
 (fn [db [_ data]]
   (assoc-in db [:modal] data)))
