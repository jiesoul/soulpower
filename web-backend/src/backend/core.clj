(ns backend.core
  (:require [aero.core :as aero]
            [backend.api :as api]
            [backend.middleware :refer [exception-middleware
                                        wrap-cors-middleware]]
            [backend.server :as server]
            [backend.util.db-util :refer [my-sql-logger]]
            [cheshire.core :refer [generate-string]]
            [clojure.java.io :as io]
            [clojure.pprint]
            [clojure.tools.logging :as log]
            [integrant.core :as ig]
            [integrant.repl :as ig-repl]
            [muuntaja.core :as mu-core]
            [next.jdbc :as jdbc]
            [next.jdbc.connection :as connection]
            [nrepl.server :as nrepl]
            [reitit.coercion.spec]
            [reitit.dev.pretty :as pretty]
            [reitit.ring :as reitit-ring]
            [reitit.ring.coercion :as reitit-coercion]
            [reitit.exception :as reitit-exception]
            [reitit.ring.middleware.muuntaja :as reitit-muuntaja]
            [reitit.ring.middleware.parameters :as reitit-parameters]
            [reitit.spec :as rs]
            [reitit.swagger :as reitit-swagger]
            [reitit.swagger-ui :as reitit-swagger-ui]
            [ring.adapter.jetty :as jetty])
  (:import (com.zaxxer.hikari HikariDataSource))
  (:gen-class))

(def contact {:name "jiesoul"
              :email "jiesoul@gmail.com"})

(def license {:name "Apache 2.0",
              :url "http://www.apache.org/licenses/LICENSE-2.0.html"})

(def version "1.0.0")

(defn routes [env]
  [["/admin" {:swagger {:id ::admin}}
    ["/swagger.json" {:no-doc true
                      :get {:swagger {:info {:title "my-api"
                                             :description "web backend api"
                                             :version version
                                             :contact contact
                                             :license license}}
                            :handler (reitit-swagger/create-swagger-handler)}}]
    ["/api-docs/*"   {:no-doc true
                      :get {:handler (reitit-swagger-ui/create-swagger-ui-handler
                                      {:config {:validatorUrl nil}
                                       :url "/admin/swagger.json"})}}]

    (server/routes env)]

   ["/api/v1" {:swagger {:id ::api}}
    ["/swagger.json" {:no-doc true
                      :get {:swagger {:info {:title "open-api"
                                             :description "public api, ex: web app,ios,android,wechat"
                                             :version version
                                             :contact contact
                                             :license license}}
                            :handler (reitit-swagger/create-swagger-handler)}}]
    ["/api-docs/*" {:no-doc true
                    :get {:handler (reitit-swagger-ui/create-swagger-ui-handler
                                    {:config {:validatorUrl nil}
                                     :url "/api/v1/swagger.json"})}}]
    (api/routes env)]])

(defn handler
  "Handler."
  [routes]
  (->
   (reitit-ring/ring-handler
    (reitit-ring/router routes
                        {:data {:muuntaja mu-core/instance
                                :coercion reitit.coercion.spec/coercion
                                :middleware [wrap-cors-middleware
                                             reitit-swagger/swagger-feature
                                             reitit-parameters/parameters-middleware
                                             reitit-muuntaja/format-middleware
                                             exception-middleware
                                                    ;; coercing response bodys
                                             reitit-coercion/coerce-response-middleware
                                                    ;; coercing request parameters
                                             reitit-coercion/coerce-request-middleware]}
                         :conflicts (fn [conflicts]
                                      (println (reitit-exception/format-exception :path-conflicts nil conflicts)))
                         :validate rs/validate
                         :exception pretty/exception})

    (reitit-ring/routes
     (reitit-ring/redirect-trailing-slash-handler)
     (reitit-ring/create-file-handler {:path "/" :root "targer/shadow/dev/resources/public"})
     (reitit-ring/create-resource-handler {:path "/"})
     (reitit-ring/create-default-handler {:not-found          (constantly {:status 404
                                                                           :body (generate-string {:error {:meesage "not found"}})})
                                          :method-not-allowed (constantly {:status  405
                                                                           :body    (generate-string {:error {:message "Method not allowed"}})})
                                          :not-acceptable     (constantly {:status  406
                                                                           :body    (generate-string {:error {:message "Totally and utterly unacceptable"}})})})))))

;; (defn env-value [key default]
;;   (some-> (or (System/getenv (name key)) default)))

(defmethod aero/reader 'ig/ref [_ _ value] (ig/ref value))

(defmethod ig/init-key :backend/hikaricp [_ options]
  (log/debug "Enter ig/ini-key :backend/hikaricp " options)
  (jdbc/with-logging
    (connection/->pool HikariDataSource options)
    my-sql-logger))

(defmethod ig/suspend-key! :backend/hikaricp [_ this]
  (log/debug "Enter ig/suspend-key! :backend/hikaricp" this)
  this)

(defmethod ig/resume-key :backend/hikaricp [_ _ _ old-impl]
  (log/debug "Enter ig/resume-key :backend/hikaricp" old-impl)
  old-impl)

(defmethod ig/halt-key! :backend/hikaricp [_ ds]
  (log/debug "Enter ig/halt-key :backend/hikaricp." ds)
  ds)

;; env
(defmethod ig/init-key :backend/profile [_ profile]
  profile)

(defmethod ig/init-key :backend/env [_ env]
  (log/debug "Enter ig/init-key :backend/env " env)
  env)

(defmethod ig/halt-key! :backend/env [_ this]
  (log/debug "Enter ig/halt-key! :backend/env" this)
  this)

(defmethod ig/suspend-key! :backend/env [_ this]
  (log/debug "Enter ig/suspend-key! :backend/env" this)
  this)

(defmethod ig/resume-key :backend/env [_ _ _ old-impl]
  (log/debug "Enter ig/resume-key :backend/env" old-impl)
  old-impl)

;; jetty web server
(defmethod ig/init-key :backend/jetty [_ {:keys [port join? env] :as options}]
  (log/debug "Enter ig/init-key :backend/jetty" options)
  (-> (handler (routes env))
      (jetty/run-jetty {:port port :join? join?})))

(defmethod ig/halt-key! :backend/jetty [_ server]
  (log/debug "Enter ig/halt-key! :backend/jetty" server)
  (.stop server))

;; init options
(defmethod ig/init-key :backend/options [_ options]
  (log/debug "Enter ig/init-key :backend/options:" options)
  options)

;; nrepl
(defmethod ig/init-key :backend/nrepl [_ {:keys [bind port]}]
  (log/debug "Enter ig/init-key :backend/nrepl" bind ":" port)
  (if (and bind port)
    (nrepl/start-server :bind bind :port port)
    nil))

(defmethod ig/halt-key! :backend/nrepl [_ this]
  (log/debug "Enter ig/halt-key! :backend/nrepl" this)
  (if this
    (nrepl/stop-server this)
    _))

(defmethod ig/suspend-key! :backend/nrepl [_ this]
  (log/debug "Enter ig/suspend-key! :backend/nrepl" this)
  this)

(defmethod ig/resume-key :backend/nrepl [_ _ _ old-impl]
  (log/debug "Enter ig/resume-key :backend/nrepl" old-impl)
  old-impl)

;; config
(defn read-config [profile]
  (log/info "Enter read config " profile)
  (let [config (aero/read-config (io/resource "config.edn") {:profile profile})]
    config))

(defn system-config [myprofile]
  (log/info "Enter system config read..." myprofile)
  (let [profile (or (keyword myprofile) (some-> (System/getenv "PROFILE") keyword) :dev)
        _ (log/info "I using profile " profile)
        config (read-config profile)]
    config))

(defn system-config-start [& myprofile]
  (log/debug "Enter system-config-start " myprofile)
  (system-config (first myprofile)))

;; main
(defn -main [& args]
  (log/info "System starting... ")
  (log/info "System args: " args)
  (let [config (system-config-start args)
        _ (log/info "Config:" config)]
    (ig-repl/set-prep! (constantly config))
    (ig-repl/go)))
