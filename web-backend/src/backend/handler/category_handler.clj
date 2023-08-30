(ns backend.handler.category-handler
  (:require [backend.db.category-db :as category-db]
            [clojure.tools.logging :as log]
            [backend.util.resp-util :as resp-util]))

(defn query-categories 
  "Query categories by condition"
  [{:keys [db]} opts]
  (log/info "Query categories " opts)
  (let [data (category-db/query-categories db opts)
        _ (log/debug "query categories data: " data)]
    (resp-util/ok data)))

(defn create-category! 
  "Create Category"
  [{:keys [db]} {:keys [name] :as category}]
  (log/info "Creatge category " category)
  (let [cs (category-db/find-by-name db name)]
    (if (seq cs) 
      (resp-util/bad-request (str "category name " name " is used!!"))
      (do 
        (category-db/create! db category)
        (resp-util/ok {} "添加成功")))))

(defn get-category 
  "Get a Category by id"
  [{:keys [db]} id]
  (log/info "Get category " id)
  (let [category (category-db/get-by-id db id)]
    (resp-util/ok category)))

(defn update-category! 
  "Update Category "
  [{:keys [db]} category]
  (log/info "Update category " category)
  (let [_ (category-db/update! db category)]
    (resp-util/ok {})))

(defn delete-category! 
  "Delete Category by id"
  [{:keys [db]} id]
  (log/info "Delete category " id)
  (let [_ (category-db/delete! db id)]
    (resp-util/ok {})))

(defn get-all-categories 
  "Get All Categories"
  [{:keys [db]}]
  (let [result (category-db/get-all-category db)]
    (resp-util/ok result)))
