(ns backend.user.login-handler
  (:require [backend.user.user-db :as user-db]
            [backend.util.req-uitl :as req-util :refer [create-token]]
            [ring.util.response :as resp]
            [buddy.hashers :as buddy-hashers]
            [clojure.tools.logging :as log]))

(defn login-auth
  "login to backend."
  [db token-options {:keys [username password]}]
  (cond
    (or (nil? username) (nil? password)) (resp/bad-request {:error {:message "用户名密码不能为空"}})
    :else
    (let [user (user-db/get-user-by-name db username)]
      (if (and user (buddy-hashers/verify password (:password user)))
        (let [_ (log/info "login User: " user)
              token (create-token (select-keys user [:id :name :roles])
                                  token-options)]
          (resp/response  (-> user
                              (dissoc :password)
                              (assoc :token token))))
        (resp/bad-request {:error {:message "用户名或密码错误"}})))))

(defn logout [_ user]
  (let [_ (log/info "User: " user " is logout")]
    (resp/response {})))
