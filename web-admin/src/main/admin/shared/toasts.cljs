(ns admin.shared.toasts 
  (:require [admin.shared.svg :as svg]
            [re-frame.core :as re-frame]))

(def t-w "flex items-center w-full max-w-xs p-2 mb-1 text-gray-500 
          bg-white rounded-lg shadow dark:text-gray-400 dark:bg-gray-800")

(def t-b "inline-flex items-center justify-center flex-shrink-0 w-10 max-w-xs h-4")

(def t-info (str t-b " text-blue-500 bg-blue-100 rounded-lg dark:bg-blue-800 dark:text-blue-200"))

(def t-success (str t-b " text-green-500 bg-green-100 rounded-lg dark:bg-green-800 dark:text-green-200"))

(def t-drange (str t-b  " text-orange-500 bg-orange-100 rounded-lg dark:bg-orange-800 dark:text-orange-200"))

(def t-warning (str t-b " text-orange-500 bg-orange-100 rounded-lg dark:bg-orange-800 dark:text-orange-200"))

(def t-c-b "ml-auto -mx-2 -my-1.5 bg-white text-red-700 hover:text-red-900 
                       rounded focus:ring-2 focus:ring-red-300 p-1.5 hover:bg-red-100 
                       inline-flex h-8 w-8 dark:text-red-500 dark:hover:text-white dark:bg-red-800 
                       dark:hover:bg-red-700")

(defn toasts []
  (let [toasts (re-frame/subscribe [:toasts])]
    (fn []
      (when-let [ts  @toasts]
        [:div {:class "fixed top-5 right-5 z-50 w-full max-w-xs"}
         (doall
          (for [id (keys ts)]
            (let [type (get-in ts [id :type])]
              [:div {:key id
                     :class t-w
                     :role "alert"}
               [:div {:class (case type
                               :success t-success
                               :warning t-warning
                               :error t-drange
                               t-info)}
                (case type
                  :success (svg/success)
                  :warning (svg/warning)
                  :error (svg/danger)
                  (svg/info))]
               [:div {:class "ml-1 text-sm font-normal w-auto h-auto overflow-y-hidden p-2"} 
                (get-in ts [id :content])]
               [:button {:type "button"
                         :class t-c-b
                         :on-click #(re-frame/dispatch [:remove-toast id])}
                (svg/close)]])))]))))