(ns admin.shared.page
  (:require [admin.shared.svg :as svg]
            [re-frame.core :as re-frame]
            [admin.util :as f-util]))

(def css-page-no-current
  "z-10 px-3 py-2 leading-tight text-blue-600 border border-blue-300 bg-blue-50 
   hover:bg-blue-100 hover:text-blue-700 dark:border-gray-700 dark:bg-gray-700 dark:text-white")

(def css-page-no 
  "block px-3 py-2 ml-0 leading-tight text-gray-500 bg-white border border-gray-300 rounded-l-lg 
   hover:bg-gray-100 hover:text-gray-700 dark:bg-gray-800 dark:border-gray-700 dark:text-gray-400 
   dark:hover:bg-gray-700 dark:hover:text-white")


(defn page-dash
  "params: page page-size total query-params event"
  [{:keys [query total] :as pg}]
  (when (pos-int? total)
    (let [{:keys [page prev-page next-page show-pages start end total total-pages]} (f-util/gen-pagination pg)]
      [:nav {:class "flex items-center justify-between pt-4 pb-4"}
       [:span {:class "text-sm font-normal text-gray-500 dark:text-gray-400"}
        "Showing "
        [:span {:class "font-semibold text-gray-900 dark:text-white"}
         (str start "-" (if (< end total) end total))]
        " of "
        [:span {:class "font-semibold text-gray-900 dark:text-white"}
         total]]
       [:ul {:class "inline-flex items-center -space-x-px"}
        (when (> page 1)
          [:li
           [:button {:on-click #(re-frame/dispatch [(:event query) (assoc query :page prev-page)])
                     :class css-page-no}
            (svg/chevron-left)]])

        (doall
         (for [p show-pages]
           [:li {:key p}
            [:button {:on-click #(re-frame/dispatch [(:event query) (assoc query :page p)])
                      :disabled (when (= p page) true)
                      :class (if (= p page) css-page-no-current css-page-no)} p]]))

        (when (< page total-pages)
          [:li
           [:button {:on-click #(re-frame/dispatch [(:event query) (assoc query :page total-pages)])
                     :class css-page-no} total-pages]]
          [:li
           [:button {:on-click #(re-frame/dispatch [(:event query) (assoc query :page next-page)])
                     :class css-page-no}
            (svg/chevron-right)]])]])))
