(ns admin.shared.modals 
  (:require [admin.shared.svg :as svg]
            [admin.state :as f-state]
            [re-frame.core :as re-frame]))

(re-frame/reg-event-db
 ::set-modal-backdrop-show?
 (fn [db [_ v]]
   (assoc-in db [:current-route :modal :back] v)))

(re-frame/reg-event-fx
 ::show-modal
 (fn [{:keys [db]} [_ key]]
   {:db (-> db (assoc-in [:current-route :modal key] true))
    :fx [[:dispatch [::set-modal-backdrop-show? true]]]}))

(re-frame/reg-event-fx 
 ::close-modal
 (fn [{:keys [db]} [_ key]]
   {:db (assoc-in db [:current-route :modal key] false)
    :fx [[:dispatch [::set-modal-backdrop-show? false]]
         [:dispatch [::f-state/clean-current-route-edit]]]}))

(defn modal-back []
  (let [modal-show? @(re-frame/subscribe [::f-state/current-modal-back?])]
    [:div {:class (str (if modal-show? "" "hidden ")
                       "bg-gray-900 bg-opacity-50 dark:bg-opacity-80 fixed inset-0 z-30")}]))

(defn modal [{:keys [id title on-close show?]} & children]
    [:div {:id id
           :tab-index "-1"
           :class (str (if show? "" "hidden ") "flex overflow-y-auto overflow-x-hidden fixed top-0 right-0 left-0 z-40 
                 justify-center items-center w-full inset-0 h-[calc(100%-1rem)] max-h-full")
           :role "dialog"}
     [:div {:class "relative p-4 w-full justify-center items-center max-w-2xl max-h-full"} 
        ;; Modal content
      [:div {:class "relative p-4 bg-white rounded-lg shadow dark:bg-gray-800 sm:p-5"}
         ;; Modal header
       [:div {:class "flex justify-between items-center pb-4 mb-4 rounded-t border-b sm:mb-5 dark:border-gray-600"} 
        [:h3 {:class "text-lg font-semibold text-gray-900 dark:text-white"}
         title]
        [:button {:type "button"
                  :class "text-gray-400 bg-transparent hover:bg-gray-200 hover:text-gray-900 rounded-lg text-sm 
                        p-1.5 ml-auto inline-flex items-center dark:hover:bg-gray-600 dark:hover:text-white"
                  :data-modal-toggle id
                  :on-click on-close}
         (svg/close)]]
        ;; Modal body
       children]]])


(defn modals-crud [title new-form edit-form delete-form]
  ;; modals
  (let [new-modal? @(re-frame/subscribe [::f-state/current-new-modal?])
        edit-modal? @(re-frame/subscribe [::f-state/current-edit-modal?])
        delete-modal? @(re-frame/subscribe [::f-state/current-delete-modal?])]
    [:div
     [modal  {:id "add-modal"
                     :show? new-modal?
                     :title (str title " Add")
                     :on-close #(re-frame/dispatch [::close-modal :new-modal?])}
      [new-form]]
     [modal  {:id "edit-modal"
                     :show? edit-modal?
                     :title (str title " Edit")
                     :on-close #(re-frame/dispatch [::close-modal :edit-modal?])}
      [edit-form]]
     [modal  {:id "delete-modal"
                     :show? delete-modal?
                     :title (str title "Delete")
                     :on-close #(re-frame/dispatch [::close-modal :delete-modal?])}
      [delete-form]]]))