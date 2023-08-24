(ns backend.api
  (:require [backend.handler.article-handler :as article-handler]
            [backend.handler.category-handler :as category-handler]
            [backend.handler.tag-handler :as tag-handler]
            [backend.util.req-uitl :as req-util]
            [clojure.spec.alpha :as s]
            [reitit.swagger :as reitit-swagger]
            [reitit.swagger-ui :as reitit-swagger-ui]))

(s/def ::page pos-int?)
(s/def ::page-size pos-int?)
(s/def ::sort string?)
(s/def ::filter string?)
(s/def ::q string?)
(s/def ::query
  (s/keys :opt-un [::page ::page-size ::sort ::filter ::q]))

;; (def asset-version "1")

(defn routes [env]

  [["/swagger.json"
    {:no-doc true
     :get {:swagger {:info {:title "my-api"
                            :description "site api"}
                     :tags [{:name "api", :description "api"}]}
           :handler (reitit-swagger/create-swagger-handler)}}]
  
   ["/api/v1"
    ["/api-docs/*" 
     {:no-doc true
      :get {:handler (reitit-swagger-ui/create-swagger-ui-handler
                      {:config {:validatorUrl nil}
                       :url "/swagger.json"})}}]
    
    [""
     ["/categories"
      {:swagger {:tags ["Public Category"]}}

      ["" {:get {:summary "get all categories"
                 :parameters {:query ::query}
                 :handler (fn [req]
                            (category-handler/get-all-categories env))}}]]

     ["/tags"
      {:swagger {:tags ["Public Tag"]}}

      ["" {:get {:summary "get hot tags"
                 :parameters {:query ::query}
                 :handler (fn [req]
                            (tag-handler/get-all-tags env))}}]]

     ["/articles"
      {:swagger {:tags ["Public Article"]}}

      ["" {:get {:summary "get rently pushed articles"
                 :parameters {:query ::query}
                 :handler (fn [req]
                            (let [query (req-util/parse-query req)]
                              (article-handler/get-pushed-articles env query)))}}]

      ["/:id" {:get {:summary "get article"
                     :parameters {:path {:id string?}}
                     :handler (fn [req]
                                (let [id (req-util/parse-path req :id)]
                                  (article-handler/get-article env id)))}}]]

     ["/archives"
      {:swagger {:tags ["archive"]}}

      ["/articles"


       ["" {:get {:summary "archive articles"
                  :handler (fn [req]
                             (article-handler/get-article-archive env req))}}]

       ["/:year" {:get {:summary "get pushed articles by year"
                        :parameters {:path {:year string?}}
                        :handler (fn [req]
                                   (let [year (req-util/parse-path req :year)]
                                     (article-handler/get-pushed-articles-by-year env year)))}}]]]]]])
