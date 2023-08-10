(ns backend.middleware.auth-middleware
  (:require [backend.db.user-db :as user-db]
            [backend.db.api-token-db :as user-token-db]
            [backend.util.req-uitl :as req-util]
            [buddy.auth :refer [authenticated?]]
            [buddy.auth.backends.token :refer [jws-backend]]
            [buddy.auth.middleware :refer [wrap-authentication
                                           wrap-authorization]]
            [buddy.core.codecs :as codecs]
            [buddy.core.nonce :as nonce]
            [clojure.string :as str]
            [ring.util.response :as resp]
            [taoensso.timbre :as log]))

;; 盐
;; (def private-key "soul")
(def secret "soulpower")
;; (def backend (backends/jws {:secret secret}))
(def auth-backend (jws-backend {:secret secret :options {:alg :hs512}}))

(defn wrap-auth-jwt [handler]
  (-> handler
      (wrap-authorization auth-backend)
      (wrap-authentication auth-backend)))

(defn random-token
  []
  (let [randomdata (nonce/random-bytes 32)]
    (codecs/bytes->hex randomdata)))

(def defautlt-valid-seconds 3600)

(defn create-user-token
  "创建 Token"
  [db user-id & {:keys [valid-seconds] :or {valid-seconds defautlt-valid-seconds}}]
  (let [create-time (java.time.Instant/now)
        expires-time (.plusSeconds create-time valid-seconds)
        token (random-token)
        _ (user-token-db/save-user-token db {:user_id user-id
                                             :token token
                                             :create_time create-time
                                             :expires_time expires-time})]
    token))

(defn my-unauthorized-handler
  [req message]
  (-> (resp/bad-request {:status :failed
                         :message message})
      (assoc :status 401)))

;; (defn my-authfn
;;   [_ token]
;;   (println (str "token: " token)))

;; (def auth-backend
;;   (backends/token-backend {:authfn my-authfn
;;                            :unauthorized-handler my-unauthorized-handler}))

(defn wrap-auth-server [handler]
  (fn [request]
    (if-not (authenticated? request)
      (my-unauthorized-handler request "登录已过期")
      (handler request))))

(defn wrap-auth [handler env role]
  (fn [request]
    (let [db (:db env)
          token (req-util/parse-header request "Token")
          user-token (user-token-db/get-user-token-by-token db token)
          now (java.time.Instant/now)]
      (log/debug "user-token: " (select-keys user-token [:user_id :token]) )
      (if (and user-token (.isAfter (java.time.Instant/parse (:expires_time user-token)) now))
        (let [user-id (:user_id user-token)
              user (user-db/get-user-by-id db user-id)
              _ (log/debug "auth user: " user-id (:username user))
              roles (-> (:roles user) (str/split #",") (set))]
          (if (contains? roles role)
            (let [id (:id user-token)
                  _ (user-token-db/update-user-token-expires-time db id (.plusSeconds now defautlt-valid-seconds))
                  _ (log/debug "update user token expires time " id)]
              (handler request))
            (my-unauthorized-handler request "用户无权限！")))
        (my-unauthorized-handler request "Token been expiresed,please relogin!!!")))))

