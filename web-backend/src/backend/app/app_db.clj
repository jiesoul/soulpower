(ns backend.app.app-db
  (:require [backend.util.db-util :as du]
            [next.jdbc :refer [unqualified-snake-kebab-opts]]
            [next.jdbc.result-set :as rs]
            [next.jdbc.sql :as sql]))

(defn query-apps [db opts]
  (let [[ws wv] (du/filter->sql opts)
        ss (du/sort->sql opts)
        [ps pv] (du/page->sql opts)
        q-sql (into [(str "select * from app "  ws ss ps)] (into wv pv))
        list (sql/query db q-sql {:builder-fn rs/as-unqualified-kebab-maps})
        t-sql (into [(str "select count(1) as c from app " ws)] wv)
        total (:c (first (sql/query db t-sql)))]
    {:list list
     :total total}))

(defn save-app! [db app]
  (sql/insert! db :app app unqualified-snake-kebab-opts))

(defn get-app-by-id [db id]
  (sql/get-by-id db :app id {:builder-fn rs/as-unqualified-maps}))

(defn delete-app-by-id! [db id]
  (sql/delete! db :app {:id id}))

(defn query-app-categories [db opts]
  (let [[ws wv] (du/filter->sql opts)
        ss (du/sort->sql opts)
        [ps pv] (du/page->sql opts)
        q-sql (into [(str "select * from app_category "  ws ss ps)] (into wv pv))
        list (sql/query db q-sql {:builder-fn rs/as-unqualified-kebab-maps})
        t-sql (into [(str "select count(1) as c from app_category " ws)] wv)
        total (:c (first (sql/query db t-sql)))]
    {:list list
     :total total}))

(defn save-app-category! [db app-category]
  (sql/insert! db :app app-category unqualified-snake-kebab-opts))

(defn get-app-category-by-id [db id]
  (sql/get-by-id db :app-category id {:builder-fn rs/as-unqualified-maps}))

(defn delete-app-category-by-id! [db id]
  (sql/delete! db :app-category {:id id}))

(defn query-app-access-logs [db opts]
  (let [[ws wv] (du/filter->sql opts)
        ss (du/sort->sql opts)
        [ps pv] (du/page->sql opts)
        q-sql (into [(str "select * from app_access_log "  ws ss ps)] (into wv pv))
        list (sql/query db q-sql {:builder-fn rs/as-unqualified-kebab-maps})
        t-sql (into [(str "select count(1) as c from app_access_log " ws)] wv)
        total (:c (first (sql/query db t-sql)))]
    {:list list
     :total total}))

(defn save-app-access-log! [db app-access-log]
  (sql/insert! db :app_access_log app-access-log unqualified-snake-kebab-opts))