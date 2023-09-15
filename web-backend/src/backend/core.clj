(ns backend.core
  (:require [aero.core :as aero]
            [backend.api :as api]
            [backend.middleware :refer [exception-middleware
                                        wrap-cors-middleware]]
            [backend.server :as server]
            [cheshire.core :refer [generate-string]]
            [clojure.java.io :as io]
            [clojure.pprint]
            [clojure.tools.logging :as log]
            [integrant.core :as ig]
            [integrant.repl :as ig-repl]
            [muuntaja.core :as mu-core]
            [nrepl.server :as nrepl]
            [reitit.coercion.spec]
            [reitit.dev.pretty :as pretty]
            [reitit.ring :as reitit-ring]
            [reitit.ring.coercion :as reitit-coercion]
            [reitit.ring.middleware.muuntaja :as reitit-muuntaja]
            [reitit.ring.middleware.parameters :as reitit-parameters]
            [reitit.spec :as rs]
            [reitit.swagger :as reitit-swagger]
            [reitit.swagger-ui :as reitit-swagger-ui]
            [ring.adapter.jetty :as jetty])
  (:gen-class))

(defn routes [env]
  [["/admin" {:swagger {:id ::admin}} (server/routes env) 
    ["/swagger.json"
     {:no-doc true
      :get {:swagger {:info {:title "my-api"
                             :description "site api"}
                      :tags [{:name "api", :description "api"}]}
            :handler (reitit-swagger/create-swagger-handler)}}]
    ["/api-docs/*"
     {:no-doc true
      :get {:handler (reitit-swagger-ui/create-swagger-ui-handler
                      {:config {:validatorUrl nil}
                       :url "/admin/swagger.json"})}}]]
   ["/api/v1" {:swagger {:id ::api}} (api/routes env)
    ["/swagger.json"
     {:no-doc true
      :get {:swagger {:info {:title "my-api"
                             :description "site api"}
                      :tags [{:name "api", :description "api"}]}
            :handler (reitit-swagger/create-swagger-handler)}}]
    ["/api-docs/*"
     {:no-doc true
      :get {:handler (reitit-swagger-ui/create-swagger-ui-handler
                      {:config {:validatorUrl nil}
                       :url "/api/v1/swagger.json"})}}]]])

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

(defmethod ig/init-key :backend/db  [_ {:keys [] :as env}]
  (log/debug "Enter ig/init-key :backend/db ")
  env)

;; env
(defmethod ig/init-key :backend/profile [_ profile]
  profile)

(defmethod ig/init-key :backend/env [_ env]
  (log/debug "Enter ig/init-key :backend/env " env)
  env)

(defmethod ig/halt-key! :backend/env [_ this]
  (log/debug "Enter ig/halt-key! :backend/env")
  this)

(defmethod ig/suspend-key! :backend/env [_ this]
  (log/debug "Enter ig/suspend-key! :backend/env")
  this)

(defmethod ig/resume-key :backend/env [_ _ _ old-impl]
  (log/debug "Enter ig/resume-key :backend/env")
  old-impl)

;; jetty web server
(defmethod ig/init-key :backend/jetty [_ {:keys [port join? env]}]
  (log/debug "Enter ig/init-key :backend/jetty")
  (-> (handler (routes env))
      (jetty/run-jetty {:port port :join? join?})))

(defmethod ig/halt-key! :backend/jetty [_ server]
  (log/debug "Enter ig/halt-key! :backend/jetty")
  (.stop server))

;; init options
(defmethod ig/init-key :backend/options [_ options]
  (log/debug"Enter ig/init-key :backend/options")
  options)

;; nrepl
(defmethod ig/init-key :backend/nrepl [_ {:keys [bind port]}]
  (log/debug "Enter ig/init-key :backend/nrepl")
  (if (and bind port)
    (nrepl/start-server :bind bind :port port)
    nil))

(defmethod ig/halt-key! :backend/nrepl [_ this]
  (log/debug "Enter ig/halt-key! :backend/nrepl")
  (if this 
    (nrepl/stop-server this)
    _))

(defmethod ig/suspend-key! :backend/nrepl [_ this]
  (log/debug "Enter ig/suspend-key! :backend/nrepl")
  this)

(defmethod ig/resume-key :backend/nrepl [_ _ _ old-impl]
  (log/debug "Enter ig/resume-key :backend/nrepl")
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
  (let [config (system-config-start (first args))
        _ (log/info "Config:" config)]
    (ig-repl/set-prep! (constantly config))
    (ig-repl/go)))