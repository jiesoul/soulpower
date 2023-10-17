(ns backend.util.resp-util
  (:require [clojure.tools.logging :as log]
            [reitit.coercion :as coercion]
            [ring.util.response :as resp]))

(defn create-coercion-handler
  "Creates a coercion exception handler."
  [status]
  (fn [e request]
    (let [message (ex-message e)
          _ (log/error "ERROR coercion trace: " (coercion/encode-error (ex-data e)))]
      {:status status
       :body {:error {:message message
                      :uri (:uri request)
                      :exception "coercion-exception"
                      :details (coercion/encode-error (ex-data e))}}})))

(defn coercion-error-handler [status]
  (let [handler (create-coercion-handler status)]
    (fn [exception request]
      (handler exception request))))

(defn handler-error [message exception request]
  (let [_ (log/error "ERROR: " exception)]
    {:status 500
     :body  {:error {:message message
                     :exception (.getClass exception)
                     :details (ex-data exception)
                     :uri (:uri request)}}}))

(defn response
  ([] (response nil))
  ([body] {:status 200
           :body body}))

(defn created
  "Returns a Ring response for a HTTP 201 created response."
  ([] (created nil))
  ([body]
   {:status  201
    :body    body}))

(defn no-content
  "Returns a Ring response for a HTTP 201 created response."
  ([] (no-content nil))
  ([body]
   {:status  204
    :body    body}))

(defn redirect [url & data]
  (resp/redirect {:status  302
                  :headers {"Location" url}
                  :data data}))

(defn bad-request [error]
  (let [error (merge {:exception "bad-request"}
                     error)
        _ (log/error "ERROR: " error)]
    {:status  400
     :body {:error  error}}))

(defn forbidden
  [uri]
  (let [error {:message "无授权的操作，请联系管理员。"
               :exception "forbidden"
               :uri uri}
        _ (log/error "ERROR: " error)]
    {:status 403
     :body {:error error}}))

(defn unauthorized
  [uri]
  (let [error {:message "用户登录已过期，请重新登录。"
               :exception "unauthorized"
               :uri uri}
        _ (log/error "ERROR: " error)]
    {:status 401
     :body {:error error}}))

(defn not-found
  "Returns a 404 'not found' response."
  [uri]
  (let [error {:message "资源不存在."
               :exception "not found"
               :uri uri}
        _ (log/error "ERROR: " error)]
    {:status  404
     :body    {:error error}}))




