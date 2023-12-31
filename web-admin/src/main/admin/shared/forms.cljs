(ns admin.shared.forms
  (:require [reagent.core :as r]
            [re-frame.core :as re-frame]
            [admin.shared.css :as css]))

(defn q-form [child]
  ;; page query form
  [:<>
    [:div {:class "flex-1 flex-col my-2 py-2 overflow-x-auto sm:-mx-6 sm:px-6 lg:-mx-8 lg:px-8"}
     [child]]
   [:div {:class "h-px my-4 bg-blue-500 border-0 dark:bg-blue-700"}]])

(defn form-base [child]
  [:div {:class "flex min-h-full flex-col justify-center px-6 py-12 lg:px-8"}
   [:div {:class "mt-10 sm:mx-auto sm:w-full sm:max-w-sm"}
    [child]]])