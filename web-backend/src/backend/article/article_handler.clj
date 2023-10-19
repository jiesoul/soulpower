(ns backend.article.article-handler
  (:require [backend.article.article-db :as article-db]
            [backend.util.resp-util :as resp-util]
            [ring.util.response :as resp]
            [clojure.tools.logging :as log]))

(defn query-articles [db opts]
  (let [data (article-db/query db opts)]
    (resp/response data)))

(defn gen-id []
  (let [now (java.time.LocalDateTime/now)
        dtf (java.time.format.DateTimeFormatter/ofPattern "yyyyMMddHHmmssSSSSS")]
    (. dtf format now)))

(defn create-article! [db article]
  (let [id (gen-id)
        _ (article-db/create! db (assoc article :id id))]
    (resp/created (str "/articles/" id))))

(defn get-article [db id]
  (let [article (article-db/get-article-and-detail-by-id db id)]
    (resp/response article)))

(defn update-article! [db id article]
  (let [_ (article-db/update! db id (-> article
                                        (assoc :push-flag 0)
                                        (assoc :push-time nil)))]
    (resp/created (str "/articles/" id))))

(defn delete-article! [db id]
  (let [_ (article-db/delete! db id)]
    (resp-util/no-content)))

(defn push! [db id article]
  (let [{:keys [push-flag]} (article-db/get-article-by-id db id)
        _ (log/debug "article status: " article)]
    (if (= push-flag 1)
      (resp/bad-request {:error {:message "Aritcle been pushed"}})
      (let [push-time (java.time.Instant/now)
            _ (article-db/push! db id (assoc article
                                             :push-time push-time
                                             :push-flag 1))]
        (resp/created (str "/articles/" id))))))

(defn get-pushed-articles [db opts]
  (let [data (article-db/get-pushed db opts)]
    (resp/response data)))

(defn get-pushed-articles-by-year [db year]
  (let [data (article-db/get-pushed-by-year db year)]
    (resp/response {(keyword year) data})))

(defn get-article-archive [db _]
  (let [data (article-db/get-archive db)]
    (resp/response data)))

(defn update-like-count! [db id c]
  (let [_ (article-db/update-article-like-count! db id c)]
    (resp/created (str "/articles/" id))))


