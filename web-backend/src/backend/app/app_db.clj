(ns backend.app.app-db
  (:require [backend.util.db-util :as du]
            [next.jdbc :refer [unqualified-snake-kebab-opts]]
            [next.jdbc.result-set :as rs]
            [next.jdbc.sql :as sql]))

(defn query-apps [db opts]
  (let [qsql " select * from app "
        tsql " select count(1) :as c from app "
        [q t] (du/query->sql opts qsql tsql)
        list (sql/query db q {:builder-fn rs/as-unqualified-kebab-maps})
        total (:c (first (sql/query db t)))]
    {:list list
     :total total}))

(defn save-app! [db app]
  (sql/insert! db :app app unqualified-snake-kebab-opts))

(defn get-app-by-id [db id]
  (sql/get-by-id db :app id {:builder-fn rs/as-unqualified-maps}))

(defn delete-app-by-id! [db id]
  (sql/delete! db :app {:id id}))

(defn query-app-categories [db opts]
  (let [q-sql "select * from app_category"
        t-sql "select count(1) as c from app_category "
        [q t] (du/query->sql opts q-sql t-sql)
        list (sql/query db q {:builder-fn rs/as-unqualified-kebab-maps})
        total (:c (first (sql/query db t)))]
    {:list list
     :total total}))

(defn save-app-category! [db app-category]
  (sql/insert! db :app-category app-category unqualified-snake-kebab-opts))

(defn get-app-category-by-id [db id]
  (sql/get-by-id db :app_category id {:builder-fn rs/as-unqualified-maps}))

(defn delete-app-category-by-id! [db id]
  (sql/delete! db :app_category {:id id}))

(defn query-app-access-logs [db opts]
  (let [q-sql "select * from app_access_log "
        t-sql "select count(1) as c from app_access_log "
        [q t] (du/query->sql opts q-sql t-sql)
        list (sql/query db q {:builder-fn rs/as-unqualified-kebab-maps})
        total (:c (first (sql/query db t)))]
    {:list list
     :total total}))

(defn save-app-access-log! [db app-access-log]
  (sql/insert! db :app_access_log app-access-log unqualified-snake-kebab-opts))
