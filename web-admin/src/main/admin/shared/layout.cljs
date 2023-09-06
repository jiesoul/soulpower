(ns admin.shared.layout
  (:require [admin.subs]
            [admin.shared.css :as css]
            [admin.shared.header :refer [header-dash]]
            [admin.shared.modals :as modals :refer [modal-back]] 
            [admin.shared.sidebar :refer [sidebar-dash]] 
            [admin.shared.toasts :refer [toasts]]
            [admin.auth.views :refer [login]]
            [re-frame.core :as re-frame]))

(defn layout-admin [modals query-form list-table]
  (let [login-user @(re-frame/subscribe [:login-user])]
    (if login-user
      [:div {:class "flex h-screen bg-gray-50 overflow-x-hidden"}
       [:div {:class "flex w-1/6 h-screen"}
        [sidebar-dash]]
       [:div {:class "flex flex-1 flex-col w-5/6"}
        [header-dash]
        [toasts]
        [:<> modals]
        [modal-back]
        [:main {:class "flex-1 bg-gray-100"} 
         [:div {:class css/main-container}
          query-form
          list-table]]]]
      [login])))
