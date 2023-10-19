(ns backend.article.comment-handler
  (:require [backend.article.comment-db :as comment-db]
            [ring.util.response :as resp]
            [clojure.tools.logging :as log]
            [backend.util.req-uitl :as req-util]))

(defn query-articles-comments [db]
  (fn [req]
    (let [opts (req-util/parse-opts req)
          articles-comments (comment-db/query db opts)]
      (resp/response articles-comments))))

(defn get-articles-comments-by-id [db]
  (fn [req]
    (let [id (req-util/parse-path req :id)
          article-comment (comment-db/get-by-id db id)]
      (resp/response article-comment))))

(defn get-comments-by-article-id [db]
  (fn [req]
    (let [article-id (req-util/parse-path req :id)
          comments (comment-db/get-comments-by-article-id db article-id)]
      (resp/response comments))))

(defn delete-comment-by-article-id! [db]
  (fn [req]
    (let [id (req-util/parse-path req :id)
          _ (comment-db/delete! db id)]
      (resp/response {}))))

(defn delete-article-comment! [db]
  (fn [req]
    (let [id (req-util/parse-path req :id)
          _ (comment-db/delete! db id)]
      (resp/response {}))))
