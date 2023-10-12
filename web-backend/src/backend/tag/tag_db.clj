(ns backend.tag.tag-db
  (:require [backend.util.db-util :as du]
            [next.jdbc.result-set :as rs]
            [next.jdbc.sql :as sql]))

(defn query [db opts]
  (let [q-sql "select * from tag"
        t-sql "select count(1) as c from tag"
        [q-sql t-sql] (du/query->sql opts q-sql t-sql)
        tags (sql/query db q-sql {:builder-fn rs/as-unqualified-maps})
        total (:c (first (sql/query db t-sql)))]
    {:list tags
     :total total}))

(defn create! [db tag]
  (sql/insert! db :tag tag))

(defn create-mutil! [db tags]
  (sql/insert! db :tag tags {:return-keys true}))

(defn update! [db tag]
  (sql/update! db :tag tag {:id (:id tag)}))

(defn delete! [db id]
  (sql/delete! db :tag {:id id}))

(defn get-by-id [db id]
  (sql/get-by-id db :tag id {:builder-fn rs/as-unqualified-maps}))

(defn get-by-name [db name]
  (first (sql/find-by-keys db :tag {:name name})))

(defn get-all-tags [db]
  (sql/query db ["select * from tag"] {:builder-fn rs/as-unqualified-kebab-maps}))

(defn get-hot-tags [db]
  (sql/query db ["select * from tag"] {:builder-fn rs/as-unqualified-kebab-maps}))
