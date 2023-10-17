(ns admin.auth.views
  (:require [admin.events]
            [admin.shared.css :as css]
            [admin.shared.form-input :refer [form-input]]
            [admin.shared.toasts :refer [toasts]]
            [admin.subs :as views]
            [re-frame.core :as re-frame]
            [reagent.core :as r]))

(defn empty-creds []
  {:username "" :password ""})

(defn login []
  (let [login-user (re-frame/subscribe [:login-user])]
    (fn []
      (let [login-data (r/atom (empty-creds))
            _ (when @login-user (re-frame/dispatch [:navigate ::views/dashboard]))
            title "Sign in to your account"]
        [:div {:class css/form-b-c}
         [toasts]
         [:div {:class css/form-b-t}
          [:h2 {:class css/form-b-t-h-2} title]]
         [:div {:class css/form-b-m}
          [:div {:class "space-y-4"}
           [form-input {:label "Username"
                        :type "text"
                        :name "username"
                        :on-change #(swap! login-data assoc :username (.. % -target -value))}]
           [form-input {:label "Password"
                        :type "password"
                        :name "password"
                        :on-change #(swap! login-data assoc :password (.. % -target -value))}]
           [:div
            [:button {:class css/btn-b-c
                      :on-click #(re-frame/dispatch [:login! @login-data])}
             "Login"]]]]]))))
