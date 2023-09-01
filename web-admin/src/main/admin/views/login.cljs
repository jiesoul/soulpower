(ns admin.views.login 
  (:require [admin.http :as f-http]
            [admin.state :as f-state]
            [re-frame.core :as re-frame]
            [admin.util :as f-util]
            [admin.shared.toasts :as toasts]
            [reagent.core :as r]))

(defn empty-creds []
  {:username "" :password ""})

(re-frame/reg-event-db
 ::save-username
 (fn [db [_ username]]
   (-> db 
       (assoc-in [:username] username))))

(re-frame/reg-sub
 ::login-response
 (fn [db]
   (:response (:login db))))

(re-frame/reg-event-db
 ::login-ret-ok
 (fn [db [_ res-body]]
   (-> db
       (assoc-in [:login :user] (:user (:data res-body)))
       (assoc-in [:login :status] :logged-in))))

(re-frame/reg-event-fx
 ::login-ret-failed
 (fn [{:keys [db]} [_ res-body]]
   {:db (-> db (assoc-in [:login] nil))
    :fx [[:dispatch [::toasts/push {:type :error :message "登录失败"}]]]}))

(re-frame/reg-event-fx
 ::f-state/login! 
 (fn [{:keys [db]} [_ user-data]]
   (f-http/http-post db (f-http/api-uri "/login") user-data [::login-ret-ok] [::login-ret-failed])))

(re-frame/reg-event-fx
 ::f-state/logout
 (fn [{:keys [db]} [_]]
   {:db (-> db
            (assoc-in [:login] nil)
            (assoc-in [:current-route] nil))
    :fx [[:dispatch [::f-state/navigate ::f-state/login]]]}))

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
      (let [_ (f-util/clog "Enter login")
            title "Login"
            {:keys [status msg error]} @(re-frame/subscribe [::login-response])
            _ (when-not error (re-frame/dispatch [::f-state/navigate ::f-state/dashboard]))] 
        [:div {:class "flex justify-center items-center h-screen bg-gray-200 px-6"}
         [:div {:class "p-6 max-w-sm w-full bg-white shadow-md rounded-md"}
          [:div {:class "flex justify-center items-center"}
           [:span {:class "text-gray-700 font-semibold text-2xl"} title]] 
          (when (= status "failed")
            [:div {:class "bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative"}
             (re-frame/dispatch [::save-username nil])
             [f-util/error-message "Login failed!" msg]])
          [:form {:class "mt-4"}  
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
                      :on-click (fn [e]
                                  (.preventDefault e)
                                  (re-frame/dispatch [::f-state/login! @login-data]))}
             "Login"]]]]]))))
