(ns backend.category.category-handler
  (:require [backend.category.category-db :as category-db]
            [ring.util.response :as resp]
            [backend.util.resp-util :as resp-util]
            [backend.util.req-uitl :as req-util]))

(defn query-categories
  "Query categories by condition"
  [db]
  (fn [req]
    (let [opts (req-util/parse-opts req)
          data (category-db/query-categories db opts)]
      (resp/response data))))

(defn create-category!
  "Create Category"
  [db]
  (fn [req]
    (let [{:keys [name] :as category} (req-util/parse-body req)
          cs (category-db/find-by-name db name)]
      (if cs
        (resp/bad-request {:error {:message (str "category name " name " is exits!!")}})
        (let [category (category-db/create! db category)]
          (resp-util/created (str "/categories/" (:id category))))))))

(defn get-category
  "Get a Category by id"
  [db]
  (fn [req]
    (let [id (req-util/parse-path req :id)]
      (if-let [category (category-db/get-by-id db id)]
        (resp/response category)
        (resp/bad-request {:error {:message "无效的ID号"}})))))

(defn update-category!
  "Update Category "
  [db]
  (fn [req]
    (let [category (req-util/parse-body req)
          rs (category-db/update! db category)]
      (if (zero? rs)
        (resp/bad-request {:error {:message "资源未找到"}})
        (resp-util/created (str "/categories/" (:id category)))))))

(defn delete-category!
  "Delete Category by id"
  [db]
  (fn [req]
    (let [id (req-util/parse-path req :id)
          rs (category-db/delete! db id)]
      (if (zero? rs)
        (resp/bad-request {:error {:message "资源未找到"}})
        (resp-util/no-content)))))

(defn get-all-categories
  "Get All Categories"
  [db]
  (fn [req]
    (let [result (category-db/get-all-category db)]
      (resp/response result))))
