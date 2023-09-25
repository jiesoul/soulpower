(ns backend.user.user-db
  (:require [backend.util.db-util :as du]
            [next.jdbc.result-set :as rs]
            [next.jdbc.sql :as sql]
            [clojure.tools.logging :as log]))

(defn query-users [db query]
  (let [qsql "select * from users"
        tsql "select count(1) as c from users"
        [q t] (du/query->sql query qsql tsql)
        users (sql/query db q {:builder-fn rs/as-unqualified-maps})
        total (:c (first (sql/query db t)))]
    {:list (map #(dissoc % :password) users) 
     :total total}))

(defn create-user!
  [db user]
  (sql/insert! db :users user))

(defn update-user! 
  [db {:keys [id] :as user}]
  (:next.jdbc/update-count (sql/update! db :users (dissoc user :id) {:id id})))

(defn update-user-password!
  [db id password]
  (:next.jdbc/update-count (sql/update! db :users {:password password} {:id id})))

(defn get-user-by-name 
  [db username]
  (first (sql/find-by-keys db :users {:username username} {:builder-fn rs/as-unqualified-maps})))

(defn get-user-by-id 
  [db id]
  (sql/get-by-id db :users id {:builder-fn rs/as-unqualified-maps}))

(defn update-user-profile! [db id user-profile]
  (log/debug "User Profile: " user-profile " id: " id)
  (:next.jdbc/update-count (sql/update! db :users user-profile {:id id})))

(defn delete-user!
  [db id]
  (:next.jdbc/update-count (sql/delete! db :users {:id id})))

