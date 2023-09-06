(ns admin.auth.events
  (:require [re-frame.core :refer [reg-event-fx after path trim-v]]
            [admin.db :refer [login->local-store remove->ocal-store]]
            [admin.http :as http]
            [admin.util :as util]))

(def set-user-interceptor [(path :login-user)
                           (after login->local-store)
                           trim-v])

(def remove-user-interceptor [(after remove->ocal-store)])

(reg-event-fx
 :login!
 (fn [{:keys [db]} [_ user-data]]
   (http/http-post (assoc-in db [:loaading :login] true) 
                   (http/api-uri "login") 
                   user-data 
                   [:login-ok]
                   [:api-request-error {:request-type :login}])))

(reg-event-fx
 :login-ok
 set-user-interceptor
 (fn [{db :db} [res-body]]
   (let [_ (util/clog "res body: " res-body)]
     {:db (-> db 
              (merge (:user (:data res-body)))
              (assoc-in [:loading :login] false))})))

(reg-event-fx
 :logout!
 remove-user-interceptor
 (fn [{:keys [db]} _]
   {:db (-> db
            (assoc-in [:login-user] nil)
            (assoc-in [:current-route] :login))}))
