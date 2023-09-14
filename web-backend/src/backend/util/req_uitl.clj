(ns backend.util.req-uitl 
  (:require [buddy.sign.jwt :as jwt]
            [clojure.tools.logging :as log]))

(def DEFAULT-PAGE 1)
(def DEFAULT-PAGE-SIZE 10)

(defn parse-header
  [request token-name]
  (log/debug "parse header request: " (:header (:parameters request)))
  (some->> (-> request :parameters :header :authorization)
           (re-find (re-pattern (str "^" token-name " (.+)$")))
           (second)))

(defn parse-body
  [req key]
  (get-in req [:body-params key]))

(defn parse-path 
  [req key]
  (get-in req [:parameters :path key]))

(defn parse-query
  [req]
  (let [query (get-in req [:parameters :query])
        page (or (get query :page) DEFAULT-PAGE)
        page-size (or (get query :page-size) DEFAULT-PAGE-SIZE)
        _ (log/debug "parameters query " query)]
    (assoc query :page page :page-size page-size)))

(def default-jwt-private-key "soulpower")
(def default-jwt-exp 3600)
(def default-jwt-options {:alg :hs512})

(defn create-token
  [user & {:keys [exp private-key]}]
  (let [payload (-> user
                    (assoc :exp (.plusSeconds
                                 (java.time.Instant/now) (or exp default-jwt-exp))))]
    (jwt/sign payload (or private-key default-jwt-private-key) default-jwt-options)))