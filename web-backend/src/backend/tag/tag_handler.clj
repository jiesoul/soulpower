(ns backend.tag.tag-handler
  (:require [backend.tag.tag-db :as tag-db]
            [clojure.tools.logging :as log]
            [ring.util.response :as resp]
            [backend.util.resp-util :as resp-util]
            [backend.util.req-uitl :as req-util]))

(defn query-tags [db]
  (fn [req]
    (let [opts (req-util/parse-opts req)
          data (tag-db/query db opts)]
      (resp/response data))))

(defn create-tag! [db]
  (fn [req]
    (let [{:keys [name] :as tag} (req-util/parse-body req)]
      (if-let [_ (tag-db/get-by-name db name)]
        (resp/bad-request {:error {:message (str "Tag: <" name "> has been created")}})
        (let [id (tag-db/create! db tag)
              _ (log/debug "create tag: " id)]
          (resp/created (str "/tags/" id)))))))

(defn get-tag [db]
  (fn [req]
    (let [id (req-util/parse-path req :id)
          tag (tag-db/get-by-id db id)]
      (resp/response tag))))

(defn update-tag! [db]
  (fn [req]
    (let [tag (req-util/parse-body req)
          _ (tag-db/update! db tag)]
      (resp/created (str "/tags/" (:id tag))))))

(defn delete-tag! [db]
  (fn [req]
    (let [id (req-util/parse-path req :id)
          _ (tag-db/delete! db id)]
      (resp-util/no-content))))

(defn get-all-tags [db]
  (let [rs (tag-db/get-all-tags db)]
    rs))

(defn get-hot-tags [db _]
  (let [rs (tag-db/get-hot-tags db)]
    rs))
