(ns backend.handler.login-handler
  (:require [backend.db.user-db :as user-db]
            [backend.util.req-uitl :as req-util :refer [create-token]]
            [backend.util.resp-util :as resp-util]
            [buddy.hashers :as buddy-hashers]
            [clojure.tools.logging :as log]))

(defn login-auth
  "login to backend."
  [db token-options {:keys [username password]}]
  (let [user (user-db/get-user-by-name db username)] 
    (if (and user (buddy-hashers/check password (:password user)))
      (let [_ (log/info "login User: " user)
            token (create-token (select-keys user [:id :name :roles]) 
                                token-options)]
        (resp-util/response  {:user (-> user
                                        (dissoc :password)
                                        (assoc :token token))}))
      (resp-util/bad-request {:message "用户名或密码错误"}))))

(defn logout [_ user]
    (let [_ (log/info "User: " user " is logout")]
      (resp-util/response)))