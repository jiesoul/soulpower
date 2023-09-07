(ns backend.handler.login-handler
  (:require [backend.db.user-db :as user-db]
            [backend.middleware.auth-middleware :as auth-middleware]
            [backend.util.resp-util :as resp-util]
            [buddy.hashers :as buddy-hashers]
            [clojure.tools.logging :as log]))

(defn login-auth
  "login to backend."
  [{:keys [db] :as env} username password]
  (log/debug "Enter login auth. username: " username " password: " password "env: " env)
  (let [user (user-db/get-user-by-name db username)] 
    (log/debug "Get a User: " user)
    (if (and user (buddy-hashers/check password (:password user))) 
      (let [_ (log/debug "login User: " user)
            token (auth-middleware/create-token (select-keys user [:id :name :roles]) (get-in env [:options :jwt]))]
        (resp-util/ok  {:user (-> user
                                  (dissoc :password)
                                  (assoc :token token))}))

      (resp-util/bad-request "用户名或密码错误"))))

(defn logout [_ user]
    (let [_ (log/info "User: " user " is logout")]
      (resp-util/ok {} "用户退出")))