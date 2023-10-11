(ns backend.category.category-handler
  (:require [backend.category.category-db :as category-db]
            [backend.util.resp-util :as resp-util]))

(defn query-categories
  "Query categories by condition"
  [db opts]
  (let [data (category-db/query-categories db opts)]
    (resp-util/response data)))

(defn create-category!
  "Create Category"
  [db {:keys [name] :as category}]
  (let [cs (category-db/find-by-name db name)]
    (if cs
      (resp-util/bad-request {:message (str "category name " name " is exits!!")})
      (do
        (category-db/create! db category)
        (resp-util/created)))))

(defn get-category
  "Get a Category by id"
  [db id]
  (if-let [category (category-db/get-by-id db id)]
    (resp-util/response category)
    (resp-util/bad-request {:message "无效的ID号"})))

(defn update-category!
  "Update Category "
  [db category]
  (let [rs (category-db/update! db category)]
    (if (zero? rs)
      (resp-util/bad-request {:message "资源未找到"})
      (resp-util/created))))

(defn delete-category!
  "Delete Category by id"
  [db id]
  (let [rs (category-db/delete! db id)]
    (if (zero? rs)
      (resp-util/bad-request {:message "资源未找到"})
      (resp-util/no-content))))

(defn get-all-categories
  "Get All Categories"
  [db]
  (let [result (category-db/get-all-category db)]
    (resp-util/response result)))
