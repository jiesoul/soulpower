(ns backend.app.app-handler
  (:require [backend.app.app-db :as app-db]
            [backend.util.common-utils :as common-utils]
            [ring.util.response :as resp]
            [backend.util.resp-util :as resp-util]
            [clojure.tools.logging :as log]
            [backend.util.req-uitl :as req-util]))

(defn query-app-categories [db]
  (fn [req]
    (let [query (req-util/parse-opts req)
          result (app-db/query-app-categories db query)]
      (resp/response result))))

(defn create-app-category! [db]
  (fn [req]
    (let [app-category (req-util/parse-body req)
          id (app-db/save-app-category! db app-category)
          uri (str "/app-categories/" id)]
      (resp-util/created uri))))

(defn get-app-category-by-id [db]
  (fn [req]
    (let [id (req-util/parse-path req :id)
          app-category (app-db/get-app-category-by-id db id)
          _ (log/debug "App Category: " app-category)]
      (if app-category
        (resp/response app-category)
        (resp/not-found {:error {:message "app category not found"}})))))

(defn delete-app-category-by-id! [db]
  (fn [req]
    (let [id (req-util/parse-path req :id)
          _ (app-db/delete-app-category-by-id! db id)]
      (resp-util/no-content))))

(defn query-apps [db]
  (fn [req]
    (let [query (req-util/parse-opts req)
          result (app-db/query-apps db query)]
      (resp/response result))))

(defn create-app! [db]
  (fn [req]
    (let [app (req-util/parse-body req)
          app-id (common-utils/gen-app-id)
          _ (log/debug "App: " app)
          app-category-id (:app-category-id app)
          app-category (app-db/get-app-category-by-id db app-category-id)]
      (if app-category
        (let [_ (app-db/save-app! db (assoc app :id app-id))
              uri (str "/app/" app-id)]
          (resp-util/created uri))
        (resp/not-found {:error {:message "app category not found"}})))))

(defn get-app-by-id [db]
  (fn [req]
    (let [id (req-util/parse-query req :id)
          app (app-db/get-app-by-id db id)]
      (if app
        (resp/response app)
        (resp/not-found {:error {:message "app not found"}})))))

(defn delete-app-by-id! [db]
  (fn [req]
    (let [id (req-util/parse-path req :id)
          _ (app-db/delete-app-by-id! db id)]
      (resp-util/no-content))))

(defn query-app-access-logs [db]
  (fn [req]
    (let [query (req-util/parse-opts req)
          result (app-db/query-app-access-logs db query)]
      (resp/response result))))

