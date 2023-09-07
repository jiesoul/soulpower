(ns admin.shared.forms
  (:require [reagent.core :as r]
            [re-frame.core :as re-frame]
            [admin.shared.css :as css]))

(defn query-form []
  ;; page query form
  
  [:<>
   [:form
    [:div {:class "flex-1 flex-col my-2 py-2 overflow-x-auto sm:-mx-6 sm:px-6 lg:-mx-8 lg:px-8"}]]
   [:div {:class "h-px my-4 bg-blue-500 border-0 dark:bg-blue-700"}]])