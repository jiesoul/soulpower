(ns backend.server
  (:require [backend.handler.article-handler :as article-handler]
            [backend.handler.auth-handler :as auth-handler]
            [backend.handler.category-handler :as category-handler]
            [backend.handler.dash-handler :as dash-handler]
            [backend.handler.tag-handler :as tag-handler]
            [backend.handler.user-handler :as user-handler]
            [backend.handler.user-token-handler :as user-token-handler]
            [backend.middleware.auth-middleware :as auth-mw]
            [backend.util.req-uitl :as req-util]
            [clojure.spec.alpha :as s]
            [clojure.tools.logging :as log]))


(s/def ::not-empty-string (s/and string? #(> (count %) 0)))
(s/def ::password-type (s/and string? #(>= (count %) 8)))

(s/def ::token (s/and string? #(re-matches #"^Token (.+)$" %)))
(s/def ::id pos-int?)
(s/def ::name ::not-empty-string)
(s/def ::description string?)

(s/def ::page pos-int?)
(s/def ::page-size pos-int?)
(s/def ::sort string?)
(s/def ::filter string?)
(s/def ::q string?)
(s/def ::query
  (s/keys :opt-un [::page ::page-size ::sort ::filter ::q]))

(s/def ::username string?)
(s/def ::nickname string?)
(s/def ::birthday string?)
(s/def ::password ::password-type)
(s/def ::age pos-int?)
(s/def ::avatar string?)
(s/def ::phone string?)
(s/def ::UserUpdate
  (s/keys :req-un [::id ::nickname ::birthday]
          :opt-un [::password ::age ::avatar ::phone]))

(s/def ::old-password ::password-type)
(s/def ::new-password ::password-type)
(s/def ::confirm-password ::password-type)
(s/def ::UserPassword
  (s/keys :req-un [::id ::old-password ::new-password ::confirm-password]))

(s/def ::CategoryAdd
  (s/keys :req-un [::name]
          :opt-un [::description]))

(s/def ::CategoryUpdate
  (s/keys :req-un [::id ::name]
          :opt-un [::description]))

(s/def ::TagAdd
  (s/keys :req-un [::name]
          :opt-un [::description]))

(s/def ::TagUpdate
  (s/keys :req-un [::id ::name]
          :opt-un [::description]))

(defn routes [env]
  [["/login" {:no-doc true
              :get {:handler (fn [req]
                               (auth-handler/login-page env req))}
              :post {:summary "login to the web site"
                     :parameters {:body {:username ::username
                                         :password ::password}}
                     :handler (fn [req]
                                (let [body (get-in req [:parameters :body])
                                      {:keys [username password]} body]
                                  (auth-handler/login-auth env username password)))}}]
   
   ["/admin" {:no-doc true
              :middleware {}}


    ["" {:get {:handler (fn [req] (dash-handler/dash-page env req))}}]

    ["/logout" {:post {:summary "user logout"
                       :handler (auth-handler/logout env)}}]



    ["/users"

     ["" {:get {:summary "Query users"
                :middleware [[auth-mw/wrap-auth env "user"]]
                :parameters {:header {:authorization ::token}
                             :query ::query}
                :handler (fn [req]
                           (let [query (req-util/parse-query req)]
                             (user-handler/query-users env query)))}}]

     ["/:id" {:get {:summary "Get a user"
                    :middleware [[auth-mw/wrap-auth env "user"]]
                    :parameters {:header {:authorization ::token}
                                 :path {:id pos-int?}}
                    :handler (fn [req]
                               (let [id (req-util/parse-path req :id)]
                                 (user-handler/get-user env id)))}

              :put {:summary "Update a user"
                    :middleware [[auth-mw/wrap-auth env "user"]]
                    :parameters {:header {:authorization ::token}
                                 :path {:id pos-int?}
                                 :body {:user ::UserUpdate}}
                    :handler (fn [req]
                               (let [user (req-util/parse-body req :user)]
                                 (user-handler/update-user! env user)))}

                ;; :delete {:summary "Delete a user"
                ;;          :middleware [[auth-mw/wrap-auth env "user"]]
                ;;          :parameters {:header {:authorization ::token}
                ;;                       :path [:map [:id pos-int?]]}
                ;;          :handler (fn [req]
                ;;                     (let [id (req-util/parse-path req :id)]
                ;;                       (user-handler/delete-user! env id)))}
              }]

     ["/:id/password" {:put {:summary "Update a user passwrod"
                             :middleware [[auth-mw/wrap-auth env "user"]]
                             :parameters {:header {:authorization ::token}
                                          :path {:id pos-int?}
                                          :body {:update-password ::UserPassword}}
                             :handler (fn [req]
                                        (let [update-password (req-util/parse-body req :update-password)]
                                          (user-handler/update-user-password! env update-password)))}}]]
    ["/users-tokens"

     ["" {:get {:summary "Query users tokens"
                :middleware [[auth-mw/wrap-auth env "user"]]
                :parameters {:header {:authorization ::token}
                             :query ::query}
                :handler (fn [req]
                           (let [query (req-util/parse-query req)]
                             (user-token-handler/query-users-tokens env query)))}}]]

    ["/categories"

     ["" {:get {:summary "Query categories"
                :middleware [[auth-mw/wrap-auth env "user"]]
                :parameters {:header {:authorization ::token}
                             :query ::query}
                :handler (fn [req]
                           (let [query (req-util/parse-query req)]
                             (category-handler/query-categories env query)))}

          :post {:summary "New a category"
                 :middleware [[auth-mw/wrap-auth env "user"]]
                 :parameters {:header {:authorization ::token}
                              :body {:category ::CategoryAdd}}
                 :handler (fn [req]
                            (let [category (req-util/parse-body req :category)]
                              (category-handler/create-category! env category)))}}]


     ["/:id" {:get {:summary "Get a category"
                    :middleware [[auth-mw/wrap-auth env "user"]]
                    :parameters {:header {:authorization ::token}
                                 :path   {:id pos-int?}}
                    :handler (fn [req]
                               (let [id (req-util/parse-path req :id)]
                                 (category-handler/get-category env id)))}

              :patch {:summary "Update a category"
                      :middleware [[auth-mw/wrap-auth env "user"]]
                      :parameters {:header {:authorization ::token}
                                   :path {:id pos-int?}
                                   :body {:category ::CategoryUpdate}}

                      :handler (fn [req]
                                 (let [category (req-util/parse-body req :category)]
                                   (category-handler/update-category! env category)))}

              :delete {:summary "Delete a category"
                       :middleware [[auth-mw/wrap-auth env "user"]]
                       :parameters {:header {:authorization ::token}
                                    :path {:id pos-int?}}
                       :handler (fn [req]
                                  (let [id (req-util/parse-path req :id)]
                                    (category-handler/delete-category! env id)))}}]]

    ["/tags"

     ["" {:get {:summary "Query tags"
                :middleware [[auth-mw/wrap-auth env "user"]]
                :parameters {:header {:authorization ::token}
                             :query ::query}
                :handler (fn [req]
                           (let [opt (req-util/parse-query req)]
                             (tag-handler/query-tags env opt)))}

          :post {:summary "New a tag"
                 :middleware [[auth-mw/wrap-auth env "user"]]
                 :parameters {:header {:authorization ::token}
                              :body {:tag ::TagAdd}}
                 :handler (fn [req]
                            (let [tag (req-util/parse-body req :tag)]
                              (tag-handler/create-tag! env tag)))}}]


     ["/:id" {:get {:summary "Get a tag"
                    :middleware [[auth-mw/wrap-auth env "user"]]
                    :parameters {:header {:authorization ::token}
                                 :path {:id pos-int?}}
                    :handler (fn [req]
                               (let [id (req-util/parse-path req :id)]
                                 (tag-handler/get-tag env id)))}

              :put {:summary "Update a tag"
                    :middleware [[auth-mw/wrap-auth env "user"]]
                    :parameters {:header {:authorization ::token}
                                 :path {:id pos-int?}
                                 :body {:tag ::TagUpdate}}
                    :handler (fn [req]
                               (let [tag (req-util/parse-body req :tag)]
                                 (tag-handler/update-tag! env tag)))}

              :delete {:summary "Delete a tag"
                       :middleware [[auth-mw/wrap-auth env "user"]]
                       :parameters {:header {:authorization ::token}
                                    :path {:id pos-int?}}
                       :handler (fn [req]
                                  (let [id (req-util/parse-path req :id)]
                                    (tag-handler/delete-tag! env id)))}}]]


    ["/articles"

     ["" {:get {:summary "Query articles"
                :middleware [[auth-mw/wrap-auth env "user"]]
                :parameters {:header {:authorization ::token}
                             :query ::query}
                :handler (fn [req]
                           (let [opt (req-util/parse-query req)]
                             (article-handler/query-articles env opt)))}

          :post {:summary "New a article"
                 :middleware [[auth-mw/wrap-auth env "user"]]
                 :parameters {:header {:authorization ::token}}
                 :handler (fn [req]
                            (log/debug  "new a article req: " (:body-params req))
                            (let [article (req-util/parse-body req :article)]
                              (article-handler/create-article! env article)))}}]


     ["/:id" {:get {:summary "Get a article"
                    :middleware [[auth-mw/wrap-auth env "user"]]
                    :parameters {:header {:authorization ::token}
                                 :path {:id string?}}
                    :handler (fn [req]
                               (let [id (req-util/parse-path req :id)]
                                 (article-handler/get-article env id)))}

              :patch {:summary "Update a article"
                      :middleware [[auth-mw/wrap-auth env "user"]]
                      :parameters {:header {:authorization ::token}
                                   :path {:id string?}}
                      :handler (fn [req]
                                 (let [article (req-util/parse-body req :article)]
                                   (article-handler/update-article! env article)))}

              :delete {:summary "Delete a article"
                       :middleware [[auth-mw/wrap-auth env "user"]]
                       :parameters {:header {:authorization ::token}
                                    :path {:id string?}}
                       :handler (fn [req]
                                  (let [id (req-util/parse-path req :id)]
                                    (article-handler/delete-article! env id)))}}]

     ["/:id/push" {:patch {:summary "Query the comments of a article"
                           :middleware [[auth-mw/wrap-auth env "user"]]
                           :parameters {:header {:authorization ::token}
                                        :path {:id string?}}
                           :handler (fn [req]
                                      (let [article (req-util/parse-body req :article)]
                                        (article-handler/push! env article)))}}]

     ["/:id/comments" {:get {:summary "Query the comments of a article"
                             :middleware [[auth-mw/wrap-auth env "user"]]
                             :parameters {:header {:authorization ::token}
                                          :path {:id string?}}
                             :handler (fn [req]
                                        (let [article-id (req-util/parse-path req :id)]
                                          (article-handler/get-comments-by-article-id env article-id)))}
                       :post {:summary "add a comments of the article"
                              :parameters {:path {:id string?}}
                              :handler (fn [req]
                                         (let [comment (req-util/parse-body req :comment)]
                                           (article-handler/save-comment! env comment)))}}]]


    ["/articles-comments"

     ["" {:get {:summary "Query all articles comments"
                :middleware [[auth-mw/wrap-auth env "user"]]
                :parameters {:header {:authorization ::token}
                             :query ::query}
                :handler (fn [req]
                           (let [query (req-util/parse-query req)]
                             (article-handler/query-articles-comments env query)))}}]

     ["/:id" {:get {:summary "Get a article comment"
                    :middleware [[auth-mw/wrap-auth env "user"]]
                    :parameters {:header {:authorization ::token}
                                 :path {:id pos-int?}}
                    :handler (fn [req]
                               (let [id (req-util/parse-path req :id)]
                                 (article-handler/get-articles-comments-by-id env id)))}

              :delete {:summary "Delete a article comment"
                       :middleware [[auth-mw/wrap-auth env "user"]]
                       :parameters {:header {:authorization ::token}
                                    :path {:id pos-int?}}
                       :handler (fn [req]
                                  (let [id (req-util/parse-path req :id)]
                                    (article-handler/delete-articles-comments-by-id env id)))}}]]]])


      ;; ["/files"
      ;;  {:swagger {:tags ["files"]}}

      ;;  ["/upload" {:post {:summary "upload a file"
      ;;                     :parameters {:multipart {:file reitit-multipart/temp-file-part}
      ;;                                  :headers {:authorization ::token}}
      ;;                     :responses {200 {:body {:file reitit-multipart/temp-file-part}}}
      ;;                     :handler (fn [{{{:keys [file]} :multipart} :parameters}]
      ;;                                {:status 200
      ;;                                 :body {:file file}})}}]

      ;;  ["/download" {:get {:summary "downloads a file"
      ;;                      :swagger {:produces ["image/png"]}
      ;;                      :parameters {:headers {:authorization ::token}}
      ;;                      :handler (fn [_]
      ;;                                 {:status 200
      ;;                                  :headers {"Content-Type" "image/png"}
      ;;                                  :body (-> "reitit.png"
      ;;                                            (io/resource)
      ;;                                            (io/input-stream))})}}]]