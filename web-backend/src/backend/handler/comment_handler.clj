(ns backend.handler.comment-handler 
  (:require [backend.db.comment-db :as comment-db]
            [backend.util.resp-util :as resp-util]
            [clojure.tools.logging :as log]))

(defn query-articles-comments [db opt]
  (log/debug "Query articles comments " opt)
  (let [articles-comments (comment-db/query db opt)]
    (resp-util/response articles-comments)))

(defn get-articles-comments-by-id [db id]
  (log/debug "Get article comment " id)
  (let [article-comment (comment-db/get-by-id db id)]
    (resp-util/response article-comment)))

(defn get-comments-by-article-id [db article-id]
  (let [_ comment-db/get-comments-by-article-id]))

(defn delete-comment-by-article-id! [db id]
  (let [_ (comment-db/delete! db id)]
    (resp-util/response {})))

(defn delete-article-comment! [db id]
  (let [_ (comment-db/delete! db id)]
    (resp-util/response {})))