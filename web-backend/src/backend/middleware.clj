(ns backend.middleware
  (:require [backend.util.req-uitl :refer [default-jwt-options
                                           default-jwt-private-key]]
            [backend.util.resp-util :refer [coercion-error-handler forbidden
                                            handler-error unauthorized]]
            [buddy.auth :refer [authenticated?]]
            [buddy.auth.backends :as backends]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [clojure.string :as str]
            [reitit.ring.middleware.exception :as exception]
            [ring.middleware.cors :refer [wrap-cors]]))

(defn wrap-cors-middleware 
  [handler]
  (wrap-cors handler
             :access-control-allow-origin [#".*"]
            ;;  :access-control-allow-headers [:content-type :authorization]
             :access-control-allow-methods [:get :put :post :patch :delete :options]))

(derive ::error ::exception)
(derive ::failure ::exception)
(derive ::horror ::exception)

(def exception-middleware
  (exception/create-exception-middleware
   (merge
    exception/default-handlers
    {:reitit.coercion/request-coercion (coercion-error-handler 400)
     :reitit.coercion/response-coercion (coercion-error-handler 500)
     ;; ex-data with :type ::error
     ::error (partial handler-error "error")

     ;; ex-data with ::exception or ::failure
     ::exception (partial handler-error "exception")

     ;; SQLException and all it's child classes
     java.sql.SQLException (partial handler-error "sql-exception")

     ;; override the default handler
     ::exception/default (partial handler-error "unknown error")

     ::exception/wrap (fn [handler e request]
                        (handler e request))})))

;; auth-middleware

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
    (if (-> req :identity :roles (str/split #",") set (contains? "admin"))
      (handler req)
      (forbidden req))))

(defn auth-middleware
  [handler]
  (fn [request]
    (if (authenticated? request)
      (handler request)
      (unauthorized request))))