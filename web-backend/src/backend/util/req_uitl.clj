(ns backend.util.req-uitl 
  (:require [buddy.sign.jwt :as jwt]))

(def DEFAULT-PAGE 1)
(def DEFAULT-PAGE-SIZE 100)

(defn parse-header
  [request token-name]
  (some->> (-> request :parameters :header :authorization)
           (re-find (re-pattern (str "^" token-name " (.+)$")))
           (second)))

(defn parse-body
  [req key]
  (get-in req [:body-params key]))

(defn parse-path 
  [req key]
  (get-in req [:parameters :path key]))

(defn push-query-filter [{:keys [filter] :as query} fs]
  (let [filter (str "( " fs " ) and (" filter ")")]
    (assoc query :filter filter)))

(defn parse-query
  [req]
  (let [query (get-in req [:parameters :query])
        page (or (get query :page) DEFAULT-PAGE)
        page-size (or (get query :page-size) DEFAULT-PAGE-SIZE)]
    (assoc query :page page :page-size page-size)))

(def default-jwt-pkey "soulpower")
(def default-jwt-exp 3600)
(def default-jwt-opts {:alg :hs512})

(defn create-token
  [data & {:keys [exp pkey]}]
  (let [payload (-> data
                    (assoc :exp (.plusSeconds
                                 (java.time.Instant/now) (or exp default-jwt-exp))))]
    (jwt/sign payload (or pkey default-jwt-pkey) default-jwt-opts)))