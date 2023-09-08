(ns admin.shared.modals 
  (:require [admin.shared.svg :as svg]
            [admin.subs]
            [admin.events]
            [re-frame.core :as re-frame]))

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
  (re-frame/dispatch [:set-modal nil]))

(defn modal-panel [{:keys [title child size]}]
    [:div {:class modal-wrapper}
     [:div {:class modal-backdrop}]
     [:div {:class modal-child
            :style {:width (case size
                             :extra-small "15%"
                             :small "30%"
                             :large "70%"
                             :extra-large "85%"
                             "100%")}
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
        [child]]]]])

(defn modal []
  (let [modal (re-frame/subscribe [:modal])]
    (fn []
      [:div 
       (when (:show? @modal)
         [modal-panel @modal])])))