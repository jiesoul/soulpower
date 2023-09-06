(ns admin.auth.views
  (:require [re-frame.core :as re-frame]
            [admin.views :as views]
            [admin.util :as util]
            [reagent.core :as r]))

(defn empty-creds []
  {:username "" :password ""})

(def css-input "form-input mt-1 block w-full rounded-md focus:border-indigo-600")

(defn form-input [{:keys [label name type on-change]}]
  [:label {:class "block mt-3"}
   [:span {:class "text-gray-700"} label]
   [:input {:class css-input
            :type type
            :name name
            :on-change on-change}]])

(defn login []
  (let [login-data (r/atom (empty-creds))]
    (fn []
      (let [_ (util/clog "Enter login")
            title "Login"
            login-user @(re-frame/subscribe [:login-user])
            _ (util/clog "login statue" login-user)
            _ (when login-user (re-frame/dispatch [:navigate ::views/dashboard]))] 
        [:div {:class "flex justify-center items-center h-screen bg-gray-200 px-6"}
         [:div {:class "p-6 max-w-sm w-full bg-white shadow-md rounded-md"}
          [:div {:class "flex justify-center items-center"}
           [:span {:class "text-gray-700 font-semibold text-2xl"} title]] 
          [:div {:class "mt-4"}  
           (form-input {:label "Username"  
                        :type "text"
                        :name "username" 
                        :on-change #(swap! login-data assoc :username (.. % -target -value))})
           (form-input {:label "Password"
                        :type "password"
                        :name "password"
                        :on-change #(swap! login-data assoc :password (.. % -target -value))})
           [:div {:class "flex justify-center items-center mt-4"}]
           [:div {:class "mt-6"}
            [:button {:class "py-2 px-4 text-center bg-indigo-600 rounded-md w-full text-white text-sm hover:bg-indigo-500"
                      :on-click #(re-frame/dispatch [:login! @login-data])}
             "Login"]]]]]))))
