(ns admin.shared.buttons
  (:require [re-frame.core :as re-frame]
            [admin.shared.modals :as modals]))

(def css-default "text-blue-700 hover:text-white border border-blue-700 hover:bg-blue-800 
                     focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg 
                     text-sm px-2 py-2 text-center inline-flex item-center mr-2 mb-2 dark:border-blue-500 
                     dark:text-blue-500 dark:hover:text-white dark:hover:bg-blue-500 
                     dark:focus:ring-blue-800")

(def css-yellow "text-yellow-400 hover:text-white border border-yellow-400 
                     hover:bg-yellow-500 focus:ring-4 focus:outline-none 
                     focus:ring-yellow-300 font-medium rounded-lg text-sm px-2 py-2 
                     text-center mr-2 mb-2 dark:border-yellow-300 dark:text-yellow-300 
                     dark:hover:text-white dark:hover:bg-yellow-400 dark:focus:ring-yellow-900")

(def css-green "text-green-400 hover:text-white border border-green-400 
                     hover:bg-green-500 focus:ring-4 focus:outline-none 
                     focus:ring-green-300 font-medium rounded-lg text-sm px-2 py-2 
                     text-center mr-2 mb-2 dark:border-green-300 dark:text-green-300 
                     dark:hover:text-white dark:hover:bg-green-400 dark:focus:ring-green-900")

(def css-red "text-red-700 hover:text-white border border-red-700 
                    hover:bg-red-800 focus:ring-4 focus:outline-none 
                    focus:ring-red-300 font-medium rounded-lg text-sm 
                    px-2 py-2 text-center mr-2 mb-2 dark:border-red-500 
                    dark:text-red-500 dark:hover:text-white dark:hover:bg-red-600 
                    dark:focus:ring-red-900")

(def btn-base "focus:ring-4 focus:outline-none focus:ring-gray-300 font-medium px-2 py-1 text-medium 
               text-center mr-2 mb-2 border hover:text-white dark:hover:text-white")

;; Button
(def button-default (str btn-base " text-blue-700 border-blue-700 hover:bg-blue-800 
                      dark:border-blue-500 dark:text-blue-500 dark:hover:bg-blue-500 dark:focus:ring-blue-800"))

(def button-dark  (str btn-base " text-gray-900 hover:text-white border-gray-800 hover:bg-gray-900 
                   dark:border-gray-600 dark:text-gray-400 dark:hover:bg-gray-600 dark:focus:ring-gray-800"))

(def button-green (str btn-base " text-green-700 hover:text-white border border-green-700 hover:bg-green-800 
                   dark:border-green-500 dark:text-green-500 dark:hover:bg-green-600 dark:focus:ring-green-800"))

(def button-red (str btn-base " text-red-700 border-red-700 hover:bg-red-800  
                               dark:border-red-500 dark:text-red-500 dark:hover:bg-red-600 dark:focus:ring-red-900"))

(def button-yellow (str btn-base "text-yellow-400 border-yellow-400 hover:bg-yellow-500 dark:border-yellow-300 
                                  dark:text-yellow-300 dark:hover:bg-yellow-400 dark:focus:ring-yellow-900"))

(def buton-purple (str btn-base " text-purple-700 border-purple-700 hover:bg-purple-800 dark:border-purple-400 
                                 dark:text-purple-400 dark:hover:bg-purple-500 dark:focus:ring-purple-900"))

(def css-edit "font-medium text-blue-600 dark:text-blue-500 hover:underline")
(def css-delete "font-medium text-red-600 dark:text-red-500 hover:underline")

(defn btn [props & children]
  (into
   [:button (merge {:type "button"
                    :class css-default} props)] children))

(defn red-button [props & children]
  (into
   [:button (merge {:type "button"
                    :class css-red}
                   props)
    children]))

(defn default-button [props & children]
  (into
   [:button (merge {:type "button"
                    :class css-default}
                   props)
    ;; (svg/search)
    children]))

(defn btn-new [{:keys [on-click]} & children]
  [:button {:type "button"
            :class button-green
            :on-click on-click}
   children])

(defn btn-query [props & child]
  (into
   [:button (merge {:type "button"
                    :class buton-purple}
                   props)
    child]))

(defn btn-edit [props & children]
  (into
   [:button (merge {:type "button"
                    :class css-edit}
                   props)
    children]))

(defn btn-del [props & children]
  (into
   [:button (merge {:type "button"
                    :class css-delete}
                   props)
    children]))

(defn link [props & children]
  [:a (merge {:class "font-medium text-blue-600 dark:text-blue-500 hover:underline"}
             props)
   children])

(defn edit-del-modal-btns [edit-fn]
  [:div
   [btn-edit {:on-click #(do
                           (re-frame/dispatch edit-fn)
                           (re-frame/dispatch [::modals/show-modal :edit-modal?]))}
    "Edit"]
   [:span " | "]
   [btn-del {:on-click #(do
                          (re-frame/dispatch edit-fn)
                          (re-frame/dispatch [::modals/show-modal :delete-modal?]))}
    "Del"]])