(ns backend.user.user-handler
  (:require [backend.user.user-db :as user-db]
            [ring.util.response :as resp]
            [buddy.hashers :as buddy-hashers]
            [clojure.instant :as instant]
            [clojure.tools.logging :as log]
            [backend.util.req-uitl :as req-util]
            [backend.util.resp-util :as resp-util]))

(defn query-users [db]
  (fn [req]
    (let [query (req-util/parse-opts req)
          result (user-db/query-users db query)]
      (resp/response result))))

(defn get-user [db]
  (fn [req]
    (let [id (req-util/parse-path req :id)
          user (user-db/get-user-by-id db id)]
      (if user
        (resp/response (dissoc user :password))
        (resp/bad-request {:error {:message "未找到用户"}})))))

(defn update-user-profile! [db]
  (fn [req]
    (let [id (req-util/parse-path req :id)
          user-profile (req-util/parse-body req)
          user-profile (-> user-profile
                           (assoc :id id)
                           (update-in [:birthday] instant/read-instant-timestamp))
          rs (user-db/update-user-profile! db id user-profile)
          _ (log/debug "update user profile result: " rs)]
      (if (zero? rs)
        (resp/bad-request {:error {:message "User not exist."}})
        (resp-util/created (str "/users/" id))))))

(defn update-user-password! [db]
  (fn [req]
    (let [id (req-util/parse-path req :id)
          {:keys [old-password new-password confirm-password]} (req-util/parse-body req)]
      (if (not= new-password confirm-password)
        (resp/bad-request {:error {:message "new password and confirm password must be same."}})
        (if (= old-password new-password)
          (resp/bad-request {:error {:message "new password and old password is same"}})
          (if-let [user (user-db/get-user-by-id db id)]
            (if (buddy-hashers/verify old-password (:password user))
              (let [_ (user-db/update-user-password! db id (buddy-hashers/derive new-password))]
                (resp/response {}))
              (resp/bad-request {:error {:message "User old password error."}}))
            (resp/bad-request {:error {:message "User not exist."}})))))))


