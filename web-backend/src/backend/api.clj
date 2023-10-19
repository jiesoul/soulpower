(ns backend.api
  (:require [backend.article.article-handler :as article-handler]
            [backend.category.category-handler :as category-handler]
            [backend.tag.tag-handler :as tag-handler]
            [backend.middleware :refer [wrap-app-auth]]
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
               :handler (category-handler/get-all-categories db)}}]]

   ["/tags" {:swagger {:tags ["Tag"]}}

    ["" {:get {:summary "get tags"
               :parameters {:top pos-int?}
               :handler (tag-handler/get-all-tags db)}}]]

   ["/articles" {:swagger {:tags ["Article"]}}

    ["" {:get {:summary "get rently pushed articles"
               :parameters {:query ::query}
               :handler (article-handler/get-pushed-articles db)}}]

    ["/archives" {:conflicting true}
     ["" {:get {:summary "Archives"
                :handler (fn [req])}}]

     ["/:year"
      ["" {:get {:summary "Archives by year"
                 :parameters {:path {:year integer?}}
                 :handler (article-handler/get-pushed-articles-by-year db)}}]

      ["/:month"
       ["" {:get {:summary "Archives by year"
                  :parameters {:path {:year integer?
                                      :month integer?}}
                  :handler (article-handler/get-pushed-articles-by-year db)}}]]]]

    ["/:id" {:conflicting true}
     ["/view" {:get {:summary "get article"
                     :parameters {:path {:id string?}}
                     :handler (article-handler/get-article db)}}]

     ["/like"
      ["" {:post {:summary "like a article"
                  :parameters {:path {:id string?}}
                  :handler (article-handler/update-like-count! db)}}]]

     ["/c"
      ["" {:post {:summary "like a article"
                  :parameters {:path {:id string?}}
                  :handler (article-handler/update-like-count! db)}}]]
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
