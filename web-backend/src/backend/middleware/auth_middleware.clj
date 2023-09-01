(ns backend.middleware.auth-middleware
  (:require [buddy.auth :refer [authenticated?]]
            [buddy.auth.backends :as backends]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [buddy.sign.jwt :as jwt]
            [clojure.tools.logging :as log]
            [clojure.string :as str]))

(def default-jwt-private-key "soulpower")
(def default-jwt-exp 3600)
(def default-jwt-options {:alg :hs512})

(defn create-token 
  [user & {:keys [exp private-key]}]
  (let [payload (-> user
                    (assoc :exp (.plusSeconds
                                 (java.time.Instant/now) (or exp default-jwt-exp))))]
    (jwt/sign payload (or private-key default-jwt-private-key) default-jwt-options)))

(defn create-token-backend [{:keys [private-key]}]
  (let [secret (or private-key default-jwt-private-key)]
  (backends/jws {:secret secret :options default-jwt-options})))

(defn create-token-auth-middleware
  [env]
  (let [token-backend (create-token-backend env)]
    (fn [handler]
      (wrap-authentication handler token-backend))))

(defn admin-middleware 
  [handler]
  (fn [req]
    (log/debug "Identity: " (-> req :identity))
    (if (-> req :identity :roles (str/split #",") set (contains? "admin"))
      (handler req)
      {:status 403 :body {:message "未授权"}})))

(defn auth-middleware
  [handler]
  (fn [request]
    (if (authenticated? request)
      (handler request)
      {:status 401 :body {:message "登录已过超时或未登录"}})))


