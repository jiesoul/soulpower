(ns admin.shared.tables 
  (:require [admin.shared.page :refer [page-dash]] 
            [re-frame.core :as re-frame]
            [admin.util :as f-util]))

(def css-list "relative shadow-md sm:rounded-lg w-full whitespace-nowrap overflow-x-auto")
(def css-list-table "w-full p-2 overflow-x-auto border-collapse border border-gray-100 text-sm text-center text-gray-500 dark:text-gray-400")
(def css-list-table-thead "text-base text-gray-700 dark:text-gray-400")
(def css-list-table-thead-tr-th "p-1 border border-gray-500 bg-gray-50 text-base leading-4 
                                 font-blod text-gray-500 tracking-wider")

(def css-list-table-tbody "bg-white")
(def css-list-table-tbody-tr "bg-white dark:bg-gray-800 dark:border-gray-700 hover:bg-gray-200")
(def css-list-table-tbody-tr-td "px-2 py-2 border whitespace-no-wrap border-gray-200")

(defn table-admin [{:keys [columns data pagination]}]
  [:div {:class css-list}
   [:table {:class css-list-table}
    [:thead {:class css-list-table-thead}
     [:tr
      (for [{:keys [data-index title key] :as column} columns]
        [:th (merge {:class css-list-table-thead-tr-th
                     :data-index data-index
                     :key key}
                    (select-keys [:key :class] column)) title])]]
    (when data
      [:tbody {:class css-list-table-tbody}
       (doall
        (for [ds data]
          [:tr {:class css-list-table-tbody-tr
                :key (str "tr-" (random-uuid))}
           (for [{:keys [key render format]} columns]
             [:td {:class css-list-table-tbody-tr-td
                   :key (str key ":" (key ds))}
              (let [v (key ds)
                    v (if format (format v) v)]
                (if-not render
                  v
                  (render ds)))])]))])]
   [page-dash pagination]])
