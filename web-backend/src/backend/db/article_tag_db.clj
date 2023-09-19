(ns backend.db.article-tag-db
  (:require [next.jdbc.sql :as sql]
            [clojure.tools.logging :as log]))

(defn create-multi! [db article-id tag-ids]
  (let [data (mapv vector (repeat article-id) tag-ids)]
    (sql/insert-multi! db :article_tag [:article_id :tag_id] data)))

(defn delete-by-article-id [db article-id]
  (:next.jdbc/update-count 
   (sql/delete! db :article_tag ["article_id = ?" article-id])))