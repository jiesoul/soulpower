(ns backend.handler.auth-handler
  (:require [backend.db.user-db :as user-db]
            [backend.middleware.auth-middleware :refer [secret]]
            [backend.util.req-uitl :as req-util]
            [backend.util.resp-util :as resp-util]
            [buddy.hashers :as buddy-hashers]
            [buddy.sign.jwt :as jwt]
            [clj-time.core :as time]
            [clojure.tools.logging :as log]))


(defn login-auth
  "login to backend."
  [{:keys [db] :as env} username password]
  (log/debug "Enter login auth. username: " username " password: " password "env: " env)
  (let [user (user-db/get-user-by-name db username)] 
    (log/debug "Get a User: " user)
    (if (and user (buddy-hashers/check password (:password user))) 
      (let [_ (log/debug "login User: " user)
            claims {:user (keyword username)
                    :exp (time/plus (time/now) (time/seconds (get-in env [:options :jwt :exp] 3600)))}
            token (jwt/sign claims secret {:alg :hs512})]
        (resp-util/ok  {:token token
                        :user (dissoc user :password)}))

      (resp-util/not-found "用户名或密码错误"))))

(defn logout [env]
  (fn [req]
    (let [db (:db env)
          token (req-util/parse-header req "Token")]
      (resp-util/ok {} "用户退出"))))