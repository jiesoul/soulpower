(ns backend.handler.user-handler
  (:require [backend.db.user-db :as user-db]
            [backend.util.req-uitl :as req-util]
            [buddy.hashers :as buddy-hashers]
            [ring.util.response :as resp]
            [clojure.tools.logging :as log]))

(defn query-users [{:keys [db]} req]
  (let [query (req-util/parse-query req)
        result (user-db/query-users db query)]
    (resp/response result)))

(defn get-user [{:keys [db]} req]
  (let [id (req-util/parse-path req :id)
        user (user-db/get-user-by-id db id)]
    (if user
      (resp/response (dissoc user :password))
      (resp/not-found {:error "User not exist."}))))

(defn update-user-profile! [{:keys [db]} req]
  (let [_ (log/debug "update user profile req: " req)
        id (req-util/parse-path req :id)
        user-profile (req-util/parse-body req :user-profile)
        db-user (user-db/get-user-by-id db id)]
    (if db-user 
      (resp/response (user-db/update-user-profile! db id user-profile))
      (resp/not-found {:error "User not exist."}))))


(defn update-user-password! [{:keys [db]} req ]
  (let [id (req-util/parse-path req :id)
        {:keys [old-password new-password confirm-password]} (req-util/parse-body req :update-password)]
    (if (not= new-password confirm-password)
      (resp/bad-request {:error "new password and confirm password must be same."})
      (if (= old-password new-password)
        (resp/bad-request {:error "new password and old password is same"})
        (if-let [user (user-db/get-user-by-id db id)]
          (if (buddy-hashers/check old-password (:password user))
            (do
              (user-db/update-user-password! db id (buddy-hashers/derive new-password))
              (resp/response {}))
            (resp/bad-request {:error "User old password error."}))
          (resp/not-found {:error "User not exist."}))))))


