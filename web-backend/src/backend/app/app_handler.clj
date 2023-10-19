(ns backend.app.app-handler
  (:require [backend.app.app-db :as app-db]
            [backend.util.common-utils :as common-utils]
            [rint.util.response :as resp]
            [backend.util.resp-util :as resp-util]
            [clojure.tools.logging :as log]))

(defn query-app-categories [db query]
  (let [result (app-db/query-app-categories db query)]
    (resp/response result)))

(defn create-app-category! [db app-category]
  (let [id (app-db/save-app-category! db app-category)
        uri (str "/app-categories/" id)]
    (resp/created uri)))

(defn get-app-category-by-id [db id]
  (let [app-category (app-db/get-app-category-by-id db id)
        _ (log/debug "App Category: " app-category)]
    (if app-category
      (resp/response app-category)
      (resp/not-found {:error {:message "app category not found"}}))))

(defn delete-app-category-by-id! [db id]
  (let [_ (app-db/delete-app-category-by-id! db id)]
    (resp-util/no-content)))

(defn query-apps [db query]
  (let [result (app-db/query-apps db query)]
    (resp/response result)))

(defn create-app! [db app]
  (let [app-id (common-utils/gen-app-id)
        _ (log/debug "App: " app)
        app-category-id (:app-category-id app)
        app-category (app-db/get-app-category-by-id db app-category-id)]
    (if app-category
      (let [_ (app-db/save-app! db (assoc app :id app-id))
            uri (str "/app/" app-id)]
        (resp/created uri))
      (resp/not-found {:error {:message "app category not found"}}))))

(defn get-app-by-id [db id]
  (let [app (app-db/get-app-by-id db id)]
    (if app
      (resp/response app)
      (resp/not-found {:error {:message "app not found"}}))))

(defn delete-app-by-id! [db id]
  (let [_ (app-db/delete-app-by-id! db id)]
    (resp-util/no-content)))

(defn query-app-access-logs [db query]
  (let [result (app-db/query-app-access-logs db query)]
    (resp/response result)))

