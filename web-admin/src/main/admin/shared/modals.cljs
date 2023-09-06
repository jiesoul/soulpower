(ns admin.shared.modals 
  (:require [admin.shared.svg :as svg]
            [admin.subs]
            [admin.events]
            [re-frame.core :as re-frame]))

(defn modal-back []
  (let [modal-show? @(re-frame/subscribe [:current-modal-back?])]
    [:div {:class (str (if modal-show? "" "hidden ")
                       "bg-gray-900 bg-opacity-50 dark:bg-opacity-80 fixed inset-0 z-30")}]))

(defn modal [{:keys [id title on-close show?]} & children]
    [:div {:id id
           :key (str "modal" id)
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
  (let [new-modal? @(re-frame/subscribe [:current-new-modal?])
        edit-modal? @(re-frame/subscribe [:current-edit-modal?])
        delete-modal? @(re-frame/subscribe [:current-delete-modal?])]
    [:div
     [modal  {:id "add-modal"
              :key (str title "-add-modal")
              :show? new-modal?
              :title (str title " Add")
              :on-close #(re-frame/dispatch [:close-modal :new-modal?])}
      [new-form]]
     [modal  {:id "edit-modal"
              :key (str title "-edit-modal")
              :show? edit-modal?
              :title (str title " Edit")
              :on-close #(re-frame/dispatch [:close-modal :edit-modal?])}
      [edit-form]]
     [modal  {:id "delete-modal"
              :key (str title "-delete-modal")
              :show? delete-modal?
              :title (str title "Delete")
              :on-close #(re-frame/dispatch [:close-modal :delete-modal?])}
      [delete-form]]]))

(def modal-wrapper "")
(def modal-backdrop "bg-gray-900 bg-opacity-50 dark:bg-opacity-80 fixed inset-0 z-30")
(def modal-child "flex overflow-y-auto overflow-x-hidden fixed top-0 right-0 left-0 z-40 
                   justify-center items-center w-full inset-0 h-[calc(100%-1rem)] max-h-full")

(def modal-content "relative p-4 w-full justify-center items-center max-w-2xl max-h-full")
(def modal-header "relative p-4 bg-white rounded-lg shadow dark:bg-gray-800 sm:p-5")
(def modal-title "flex justify-between items-center pb-4 mb-4 rounded-t border-b sm:mb-5 dark:border-gray-600")
(def modal-title-h3 "text-lg font-semibold text-gray-900 dark:text-white")
(def modal-close "text-gray-400 bg-transparent hover:bg-gray-200 hover:text-gray-900 rounded-lg text-sm 
                          p-1.5 ml-auto inline-flex items-center dark:hover:bg-gray-600 dark:hover:text-white")

(defn- close-modal []
  (re-frame/dispatch [:modal {:show? false :child nil}]))

(defn modal-panel [{:keys [title child size show? on-close]}]
  [:div {:class modal-wrapper}
   [:div {:class modal-backdrop
          :on-click (fn [event]
                      (do 
                        (re-frame/dispatch [:modal {:show? (not show?)
                                           :child nil
                                           :size :default}])
                        (.preventDefault event)
                        (.stopPropagation event)))}]
   [:div {:class modal-child
          :style {:width (case size 
                           :extra-small "15%"
                           :small "30%"
                           :large "70%"
                           :extra-large "85%"
                           "50%")}
          :role "dialog"}
    [:div {:class modal-content}
          ;; Modal content
     [:div {:class modal-header}
           ;; Modal header
      [:div {:class modal-title}
       [:h3 {:class modal-title-h3} title]
       [:button {:type "button"
                 :class modal-close
                 :on-click #(close-modal)}
        (svg/close)]]
          ;; Modal body
      child]]]])

(defn modals []
  (let [modal (re-frame/subscribe [:modal])]
    (fn []
      [:div 
       (if (:show? @modal)
         [modal-panel @modal])])))