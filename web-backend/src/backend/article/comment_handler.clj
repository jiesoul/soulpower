(ns backend.article.comment-handler
  (:require [backend.article.comment-db :as comment-db]
            [ring.util.response :as resp]
            [clojure.tools.logging :as log]))

(defn query-articles-comments [db opt]
  (log/debug "Query articles comments " opt)
  (let [articles-comments (comment-db/query db opt)]
    (resp/response articles-comments)))

(defn get-articles-comments-by-id [db id]
  (log/debug "Get article comment " id)
  (let [article-comment (comment-db/get-by-id db id)]
    (resp/response article-comment)))

(defn get-comments-by-article-id [db article-id]
  (let [comments (comment-db/get-comments-by-article-id db article-id)]
    (resp/response comments)))

(defn delete-comment-by-article-id! [db id]
  (let [_ (comment-db/delete! db id)]
    (resp/response {})))

(defn delete-article-comment! [db id]
  (let [_ (comment-db/delete! db id)]
    (resp/response {})))
