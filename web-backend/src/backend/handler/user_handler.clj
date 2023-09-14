(ns backend.handler.user-handler
  (:require [backend.db.user-db :as user-db]
            [backend.util.req-uitl :as req-util]
            [backend.util.resp-util :as resp-util]
            [buddy.hashers :as buddy-hashers]))

(defn query-users [{:keys [db]} req]
  (let [query (req-util/parse-query req)
        result (user-db/query-users db query)]
    (resp-util/response result)))

(defn get-user [{:keys [db]} req]
  (let [id (req-util/parse-path req :id)
        user (user-db/get-user-by-id db id)]
    (if user
      (resp-util/response {:user (dissoc user :password)})
      (resp-util/bad-request {:message "未找到用户"}))))

(defn update-user-profile! [{:keys [db]} req]
  (let [id (req-util/parse-path req :id)
        user-profile (req-util/parse-body req :user-profile)
        rs (user-db/update-user-profile! db id user-profile)]
    (if (= 1 (val rs)) 
      (resp-util/created {})
      (resp-util/bad-request {:message "User not exist."}))))

(defn update-user-password! [{:keys [db]} req ]
  (let [id (req-util/parse-path req :id)
        {:keys [old-password new-password confirm-password]} (req-util/parse-body req :update-password)]
    (if (not= new-password confirm-password)
      (resp-util/bad-request {:message "new password and confirm password must be same."})
      (if (= old-password new-password)
        (resp-util/bad-request {:message "new password and old password is same"})
        (if-let [user (user-db/get-user-by-id db id)]
          (if (buddy-hashers/check old-password (:password user))
            (do
              (user-db/update-user-password! db id (buddy-hashers/derive new-password))
              (resp-util/response {}))
            (resp-util/bad-request {:message "User old password error."}))
          (resp-util/bad-request {:message "User not exist."}))))))


