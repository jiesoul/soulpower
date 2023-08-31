(ns backend.middleware
  (:require [clojure.tools.logging :as log]
            [reitit.ring.middleware.exception :as exception]
            [ring.middleware.cors :refer [wrap-cors]]))

(defn wrap-cors-middleware 
  [handler]
  (wrap-cors handler
             :access-control-allow-origin [#".*"]
            ;;  :access-control-allow-headers [:content-type :authorization]
             :access-control-allow-methods [:get :put :post :patch :options]))

(derive ::error ::exception)
(derive ::failure ::exception)
(derive ::horror ::exception)

(defn handler [message exception request]
  (log/error "ERROR uri: " (pr-str (:uri request)))
  (log/error "ERROR trace: " exception) 
  {:status 500 
   :body  {:message message 
           :exception (.getClass exception)
           :data (ex-data exception)
           :uri (:uri request)}})

(def exception-middleware
  (exception/create-exception-middleware
   (merge
    exception/default-handlers
    {;; ex-data with :type ::error
     ::error (partial handler "error")

     ;; ex-data with ::exception or ::failure
     ::exception (partial handler "exception")

     ;; SQLException and all it's child classes
     java.sql.SQLException (partial handler "sql-exception")

     ;; override the default handler
     ::exception/default (partial handler "unknown error")

     ::exception/wrap (fn [handler e request] 
                        (handler e request))})))