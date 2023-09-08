(ns admin.events
  (:require [admin.db :refer [default-db MAX-TIMEOUT]]
            [re-frame.core :refer [reg-event-db reg-event-fx inject-cofx reg-fx dispatch subscribe]]
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
 :navigate!
 (fn [route]
   (apply rfe/push-state route)))

(reg-fx 
 :timeout 
 (fn [{:keys [event time]}]
   (js/setTimeout 
    (fn []
      (dispatch event))
    time)))

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
   {:navigate! route}))

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

;; toasts

(reg-event-fx
 :push-toast
 [(inject-cofx :uuid)]
 (fn [{:keys [db uuid]} [_ t]]
   {:db (assoc-in db [:toasts uuid] t)
    :timeout {:event [:remove-toast uuid]
              :time MAX-TIMEOUT}}))

(reg-event-db
 :remove-toast
 (fn [db [_ id]]
   (update-in db [:toasts] dissoc id)))

;; server error handler
(reg-event-fx
 :req-failed-message
 (fn [{:keys [db]} [_ {:keys [response]}]]
   (util/clog "resp failed: " response)
   {:db (assoc-in db [:loading] false)
    :fx [[:dispatch [:push-toast {:content (:message response)
                                  :type :error}]]]}))

(reg-event-db
 :set-modal
 (fn [db [_ data]]
   (assoc-in db [:modal] data)))
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


