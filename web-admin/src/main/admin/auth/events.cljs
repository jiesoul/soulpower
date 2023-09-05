(ns admin.auth.events
  (:require [re-frame.core :refer [reg-event-fx reg-event-db after]]
            [admin.db :refer [login->local-store]]
            [admin.views :as views]
            [admin.http :as http]
            [admin.util :as util]))

(def login-interceptors (after login->local-store))

(reg-event-db
 :login-ok
 (fn [db [_ res-body]]
   (let [_ (util/clog "res body: " res-body)
         login {:user (:user (:data res-body))
                :status :logged-in}
         _ (login->local-store login)]
     (-> db
         (assoc :login login)))))

(reg-event-fx
 :login-failed
 (fn [{:keys [db]} [_ res-body]]
   {:db (-> db (assoc-in [:login] nil))
    :fx [[:dispatch [:push-toast {:type :error 
                                  :message "登录失败"}]]]}))

(reg-event-fx
 :login!
 (fn [{:keys [db]} [_ user-data]]
   (http/http-post db (http/api-uri "/login") user-data [:login-ok])))

(reg-event-fx
 :logout!
 (fn [{:keys [db]} [_]]
   {:db (-> db
            (assoc-in [:login] nil)
            (assoc-in [:current-route] nil))
    :fx [[:dispatch [:navigate ::views/login]]]}))