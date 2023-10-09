(ns backend.category.category-db
  (:require [next.jdbc.sql :as sql]
            [backend.util.db-util :as du]
            [clojure.tools.logging :as log]
            [next.jdbc.result-set :as rs]))

(defn query-categories [db opts]
  (try
    (let [q-sql "select * from category "
          t-sql "select count(1) as c from category "
          [q t] (du/page->sql opts q-sql t-sql)
          categories (sql/query db q {:builder-fn rs/as-unqualified-maps})
          total (:c (first (sql/query db t)))]
      {:list categories
       :total total})
    (catch java.sql.SQLException se
      (throw (ex-info "query error" se)))))

(defn create! [db category]
  (try
    (sql/insert! db :category category {:return-keys true})
    (catch java.sql.SQLException se
      (throw (ex-info "create category error" se)))))

(defn update! [db category]
  (:next.jdbc/update-count (sql/update! db :category category {:id (:id category)})))

(defn delete! [db id]
  (:next.jdbc/update-count (sql/delete! db :category {:id id})))

(defn get-by-id [db id]
  (sql/get-by-id db :category id {:builder-fn rs/as-unqualified-kebab-maps}))

(defn find-by-name [db name]
  (first (sql/find-by-keys db :category {:name name} {:builder-fn rs/as-unqualified-kebab-maps})))

(defn get-all-category [db]
  (sql/query db ["select * from category"] {:builder-fn rs/as-unqualified-kebab-maps}))
