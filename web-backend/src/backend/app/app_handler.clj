(ns backend.app.app-handler
  (:require [backend.app.app-db :as app-db]
            [backend.util.common-utils :as common-utils]
            [backend.util.resp-util :as resp-util]))

(defn query-apps [db query]
  (let [result (app-db/query-apps db query)]
    (resp-util/response result)))

(defn create-app! [db app]
  (let [app-id (common-utils/gen-app-id)
        _ (app-db/save-app! db (assoc app :id app-id))]
    (resp-util/created)))

(defn get-app-by-id [db id]
  (let [app (app-db/get-app-by-id db id)]
    (resp-util/response app)))

(defn query-app-categories [db query]
  (let [result (app-db/query-app-categories db query)]
    (resp-util/response result)))

(defn create-app-category! [db app-category]
  (let [_ (app-db/save-app-category! db app-category)]
    (resp-util/created)))

(defn get-app-category-by-id [db id]
  (let [app-category (app-db/get-app-category-by-id db id)]
    (resp-util/response app-category)))

(defn delete-app-category-by-id! [db id]
  (let [_ (app-db/delete-app-category-by-id! db id)]
    (resp-util/no-content)))

(defn query-app-access-logs [db query]
  (let [result (app-db/query-app-access-logs db query)]
    (resp-util/response result)))

