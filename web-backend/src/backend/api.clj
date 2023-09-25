(ns backend.api
  (:require [backend.article.article-handler :as article-handler]
            [backend.category.category-handler :as category-handler]
            [backend.tag.tag-handler :as tag-handler]
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
     ["/categories" {:swagger {:tags ["Category"]}}

      ["" {:get {:summary "get categories"
                 :parameters {:top pos-int?
                              :query ::query}
                 :handler (fn [req]
                            (let [top (req-util/parse-path req :top)]
                              (category-handler/get-all-categories db)))}}]]

     ["/tags" {:swagger {:tags ["Tag"]}}

      ["" {:get {:summary "get tags"
                 :parameters {:top pos-int?}
                 :handler (fn [req]
                            (let [top (req-util/parse-path req :top)]
                              (tag-handler/get-all-tags db)))}}]]

     ["/articles" {:swagger {:tags ["Article"]}}

      ["" {:get {:summary "get rently pushed articles"
                 :parameters {:query ::query}
                 :handler (fn [req]
                            (let [query (req-util/parse-default-page req)]
                              (article-handler/get-pushed-articles db query)))}}]

      ["/archives" {:conflicting true}
       ["" {:get {:summary "Archives"
                  :handler (fn [req])}}]
       
       ["/:year" 
        ["" {:get {:summary "Archives by year"
                   :parameters {:path {:year integer?}}
                   :handler (fn [req]
                              (let [year (req-util/parse-path req :year)]
                                (article-handler/get-pushed-articles-by-year db year)))}}]

        ["/:month" 
         ["" {:get {:summary "Archives by year"
                    :parameters {:path {:year integer?
                                        :month integer?}}
                    :handler (fn [req]
                               (let [month (req-util/parse-path req :month)]
                                 (article-handler/get-pushed-articles-by-year db month)))}}]]]]

      ["/:id" {:conflicting true}
       ["" {:get {:summary "get article"
                  :parameters {:path {:id string?}}
                  :handler (fn [req]
                             (let [id (req-util/parse-path req :id)]
                               (article-handler/get-article db id)))}}]

       ["/like"
        ["" {:post {:summary "like a article"
                    :parameters {:path {:id string?}}
                    :handler (fn [req]
                               (let [id (req-util/parse-path req :id)]
                                 (article-handler/update-like-count! db id 1)))}}]]

       ["/comments"
        ["" {:get {:summary "get the comments of article"
                   :handler (fn [req])}}]]]]
     
     ["/comments" {:swagger {:tags ["Comment"]}}

      ["" {:post {:summary "create a comments of article"
                  :handler (fn [req])}}]

      ["/:id"
       ["/like" 
        ["" {:post {:summary "like a comments"
                    :handler (fn [req])}}]]]]])
