(ns backend.api
  (:require [backend.handler.article-handler :as article-handler]
            [backend.handler.category-handler :as category-handler]
            [backend.handler.tag-handler :as tag-handler]
            [backend.middleware :refer [wrap-app-auth]]
            [backend.util.req-uitl :as req-util]
            [clojure.spec.alpha :as s]))

(s/def ::appid string?)
(s/def ::page pos-int?)
(s/def ::page-size pos-int?)
(s/def ::sort string?)
(s/def ::filter string?)
(s/def ::q string?)
(s/def ::query 
  (s/keys :req-un [::appid]))

;; (def asset-version "1")

(defn routes [{:keys [db]}]
    ["" {:middleware [[wrap-app-auth db]]}
     ["/categories"
      {:swagger {:tags ["Public Category"]}}

      ["/hot/:top" {:get {:summary "get hot categories"
                          :parameters {:top pos-int?
                                       :query ::query}
                          :handler (fn [req]
                                     (let [top (req-util/parse-path req :top)]
                                       (category-handler/get-all-categories db)))}}]]

     ["/tags"
      {:swagger {:tags ["Public Tag"]}}

      ["/hot/:top" {:get {:summary "get hot tags"
                          :parameters {:top pos-int?}
                          :handler (fn [req]
                                     (let [top (req-util/parse-path req :top)]
                                       (tag-handler/get-all-tags db)))}}]]

     ["/articles"
      {:swagger {:tags ["Public Article"]}}

      ["" {:get {:summary "get rently pushed articles"
                 :parameters {:query ::query}
                 :handler (fn [req]
                            (let [query (req-util/parse-query req)]
                              (article-handler/get-pushed-articles db query)))}}
       ["/archives"
        {:swagger {:tags ["archive"]}}

        ["/articles"

         ["/:year" {:get {:summary "get pushed articles by year"
                          :parameters {:path {:year integer?}}
                          :handler (fn [req]
                                     (let [year (req-util/parse-path req :year)]
                                       (article-handler/get-pushed-articles-by-year db year)))}}]]]]

      ["/:id" {:get {:summary "get article"
                     :parameters {:path {:id string?}}
                     :handler (fn [req]
                                (let [id (req-util/parse-path req :id)]
                                  (article-handler/get-article db id)))}}
       
       ["/like" {:post {:summary "like article"
                        :handler (fn [req]
                                   (let [id (req-util/parse-path req :id)]
                                     (article-handler/update-like-count! db id 1)))}}]]]])
