(ns backend.user.user-handler
  (:require [backend.user.user-db :as user-db]
            [ring.util.response :as resp]
            [buddy.hashers :as buddy-hashers]
            [clojure.instant :as instant]
            [clojure.tools.logging :as log]))

(defn query-users [db query]
  (let [result (user-db/query-users db query)]
    (resp/response result)))

(defn get-user [db id]
  (let [user (user-db/get-user-by-id db id)]
    (if user
      (resp/response (dissoc user :password))
      (resp/bad-request {:error {:message "未找到用户"}}))))

(defn update-user-profile! [db id user-profile]
  (let [user-profile (-> user-profile
                         (update-in [:birthday] instant/read-instant-timestamp))
        rs (user-db/update-user-profile! db id user-profile)
        _ (log/debug "update user profile result: " rs)]
    (if (zero? rs)
      (resp/bad-request {:error {:message "User not exist."}})
      (resp/created (str "/users/" id)))))

(defn update-user-password! [db id {:keys [old-password new-password confirm-password]}]
  (if (not= new-password confirm-password)
    (resp/bad-request {:error {:message "new password and confirm password must be same."}})
    (if (= old-password new-password)
      (resp/bad-request {:error {:message "new password and old password is same"}})
      (if-let [user (user-db/get-user-by-id db id)]
        (if (buddy-hashers/verify old-password (:password user))
          (let [_ (user-db/update-user-password! db id (buddy-hashers/derive new-password))]
            (resp/response {}))
          (resp/bad-request {:error {:message "User old password error."}}))
        (resp/bad-request {:error {:message "User not exist."}})))))


