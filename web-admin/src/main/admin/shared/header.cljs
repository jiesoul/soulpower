(ns admin.shared.header 
  (:require [cljs.pprint]
            [admin.shared.css :as css]
            [admin.events]
            [admin.subs]
            [re-frame.core :as re-frame]
            [reagent.core :as r]))

(def css-user-dropdown-li-a "block px-4 py-2 hover:bg-gray-100 dark:hover:bg-gray-600 dark:hover:text-white")
(def user-dropdown-show? (r/atom true))

(defn user-dropdown []
  [:div {:id "user-dropdown"
         :hidden @user-dropdown-show?
         :class "fixed z-20 right-10 bg-white divide-y divide-gray-100 rounded-lg shadow w-44 
                 dark:bg-gray-700 dark:divide-gray-600"}
   [:div {:class "px-4 py-3 text-sm text-gray-900 dark:text-white"}
    [:ul {:class "py-2 text-sm text-gray-700 dark:text-gray-200"}
     [:li>a {:class css-user-dropdown-li-a
             :href "#"}
      "Dashboard"]
     [:li>a {:class css-user-dropdown-li-a
             :href "#"}
      "Setting"]]
    [:div {:class "py-1"}
     [:a {:href "#"
          :class "block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 dark:hover:bg-gray-600 
                  dark:text-gray-200 dark:hover:text-white"
          :on-click #(re-frame/dispatch [:logout!])}
      "Sign out"]]]])

(defn header-dash [] 
  (let [login-user @(re-frame/subscribe [:login-user])
        current-route @(re-frame/subscribe [:current-route])]
    [:header {:class "flex items-center justify-between px-2 py-4 bg-white border-b border-indigo-600"}
     [:div {:class "flex items-center"}
      [:div {:class "relative mx-4 lg:mx-0"}
       [:h5 {:class css/page-title} (get-in current-route [:data :link-text])]
       [:span {:class "absolute inset-y-0 left-0 flex items-center pl-1"}]]] 
     [:div {:class "flex items-center space-x-4"}
      (when login-user
        [:div {:class "font-medium dark:text-white"}
         [:p {:class ""
              :href "#"} 
          (when login-user (:username login-user))]
         (user-dropdown)])]]))