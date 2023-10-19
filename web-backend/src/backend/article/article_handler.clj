(ns backend.article.article-handler
  (:require [backend.article.article-db :as article-db]
            [backend.util.req-uitl :as req-util]
            [backend.util.resp-util :as resp-util]
            [ring.util.response :as resp]
            [clojure.tools.logging :as log]))

(defn query-articles [db]
  (fn [req]
    (let [opts (req-util/parse-opts req)
          data (article-db/query db opts)]
      (resp/response data))))

(defn gen-id []
  (let [now (java.time.LocalDateTime/now)
        dtf (java.time.format.DateTimeFormatter/ofPattern "yyyyMMddHHmmssSSSSS")]
    (. dtf format now)))

(defn create-article! [db]
  (fn [req]
    (let [id (gen-id)
          article (req-util/parse-body req)
          _ (article-db/create! db (assoc article :id id))]
      (resp/created (str "/articles/" id)))))

(defn get-article [db]
  (fn [req]
    (let [id (req-util/parse-path req :id)
          article (article-db/get-article-and-detail-by-id db id)]
      (resp/response article))))

(defn update-article! [db]
  (fn [req]
    (let [id (req-util/parse-path req :id)
          article (req-util/parse-body req)
          _ (article-db/update! db id (-> article
                                          (assoc :push-flag 0)
                                          (assoc :push-time nil)))]
      (resp/created (str "/articles/" id)))))

(defn delete-article! [db]
  (fn [req]
    (let [id (req-util/parse-path req :id)
          _ (article-db/delete! db id)]
      (resp-util/no-content))))

(defn push! [db]
  (fn [req]
    (let [id (req-util/parse-path req :id)
          article (req-util/parse-opts req)
          {:keys [push-flag]} (article-db/get-article-by-id db id)
          _ (log/debug "article status: " article)]
      (if (= push-flag 1)
        (resp/bad-request {:error {:message "Aritcle been pushed"}})
        (let [push-time (java.time.Instant/now)
              _ (article-db/push! db id (assoc article
                                               :push-time push-time
                                               :push-flag 1))]
          (resp/created (str "/articles/" id)))))))

(defn get-pushed-articles [db]
  (fn [req]
    (let [opts (req-util/parse-opts req)
          data (article-db/get-pushed db opts)]
      (resp/response data))))

(defn get-pushed-articles-by-year [db]
  (fn [req]
    (let [year (req-util/parse-path req :year)
          data (article-db/get-pushed-by-year db year)]
      (resp/response {(keyword year) data}))))

(defn get-article-archive [db]
  (fn [req]
    (let [data (article-db/get-archive db)]
      (resp/response data))))

(defn update-like-count! [db]
  (fn [req]
    (let [id (req-util/parse-path req :id)
          c (req-util/parse-body req :like)
          _ (article-db/update-article-like-count! db id c)]
      (resp/created (str "/articles/" id)))))


