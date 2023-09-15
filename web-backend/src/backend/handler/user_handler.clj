(ns backend.handler.user-handler
  (:require [backend.db.user-db :as user-db]
            [backend.util.resp-util :as resp-util]
            [buddy.hashers :as buddy-hashers]
            [clojure.instant :as instant]
            [clojure.tools.logging :as log]))

(defn query-users [{:keys [db]} query]
  (let [result (user-db/query-users db query)]
    (resp-util/response result)))

(defn get-user [{:keys [db]} id]
  (let [user (user-db/get-user-by-id db id)]
    (if user
      (resp-util/response {:user (dissoc user :password)})
      (resp-util/bad-request {:message "未找到用户"}))))

(defn update-user-profile! [{:keys [db]} id user-profile]
  (let [user-profile (-> user-profile
                         (update-in [:birthday] instant/read-instant-timestamp))
        rs (user-db/update-user-profile! db id user-profile)
        _ (log/debug "update user profile result: " rs)]
    (if (zero? rs)
      (resp-util/bad-request {:message "User not exist."})
      (resp-util/created))))

(defn update-user-password! [{:keys [db]} id {:keys [old-password new-password confirm-password]}]
  (if (not= new-password confirm-password)
    (resp-util/bad-request {:message "new password and confirm password must be same."})
    (if (= old-password new-password)
      (resp-util/bad-request {:message "new password and old password is same"})
      (if-let [user (user-db/get-user-by-id db id)]
        (if (buddy-hashers/check old-password (:password user))
          (do
            (user-db/update-user-password! db id (buddy-hashers/derive new-password))
            (resp-util/created))
          (resp-util/bad-request {:message "User old password error."}))
        (resp-util/bad-request {:message "User not exist."})))))


