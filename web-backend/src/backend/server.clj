(ns backend.server
  (:require [backend.app.app-handler :as app-handler]
            [backend.article.article-handler :as article-handler]
            [backend.category.category-handler :as category-handler]
            [backend.article.comment-handler :as article-comment-handler]
            [backend.user.login-handler :as login-handler]
            [backend.tag.tag-handler :as tag-handler]
            [backend.user.user-handler :as user-handler]
            [backend.middleware :refer [admin-middleware auth-middleware
                                        create-token-auth-middleware]]
            [backend.spec]
            [clojure.java.io :as io]
            [reitit.ring.middleware.multipart :as reitit-multipart]))

(defn routes [{:keys [options db]}]
  (let [{:keys [jwt-opts no-doc]} options
        no-doc (or no-doc false)]
    [["/login" {:no-doc no-doc
                :swagger {:tags ["Login"]}}

      ["" {:post {:summary "login to the web site"
                  :parameters {:body :bs/login-user}
                  :handler (login-handler/login-auth db jwt-opts)}}]]

     ["" {:no-doc no-doc
          :middleware [(create-token-auth-middleware jwt-opts)
                       auth-middleware
                       admin-middleware]
          :parameters {:header {:authorization :bs/token}}}

      ["/users" {:swagger {:tags ["User"]}}

       ["" {:get {:summary "Query users"
                  :parameters {:query :bs/query}
                  :handler (user-handler/query-users db)}}]

       ["/:id" {:parameters {:path {:id pos-int?}}}

        ["" {:get {:summary "Get a user"
                   :handler (user-handler/get-user db)}

             :patch {:summary "Update user profile"
                     :parameters {:body :bs/UserProfile}
                     :handler (user-handler/update-user-profile! db)}}]

        ["/password"
         ["" {:patch {:summary "Update a user passwrod"
                      :parameters {:body :bs/UpdatePassword}
                      :handler (user-handler/update-user-password! db)}}]]]]

      ["/categories" {:swagger {:tags ["Categories"]}}

       ["" {:get {:summary "Query categories"
                  :parameters {:query :bs/query}
                  :handler (category-handler/query-categories db)}

            :post {:summary "New a category"
                   :parameters {:body :bs/Category}
                   :handler (category-handler/create-category! db)}}]

       ["/:id" {:get {:summary "Get a category"
                      :parameters {:path   {:id pos-int?}}
                      :handler (category-handler/get-category db)}

                :patch {:summary "Update a category"
                        :parameters {:path {:id pos-int?}
                                     :body :bs/Category}
                        :handler (category-handler/update-category! db)}

                :delete {:summary "Delete a category"
                         :parameters {:path {:id pos-int?}}
                         :handler (category-handler/delete-category! db)}}]]

      ["/tags" {:swagger {:tags ["Tags"]}}

       ["" {:get {:summary "Query tags"
                  :parameters {:query :bs/query}
                  :handler (tag-handler/query-tags db)}

            :post {:summary "New a tag"
                   :parameters {:body :bs/Tag}
                   :handler (tag-handler/create-tag! db)}}]

       ["/:id" {:get {:summary "Get a tag"
                      :parameters {:path {:id pos-int?}}
                      :handler (tag-handler/get-tag db)}

                :put {:summary "Update a tag"
                      :parameters {:path {:id pos-int?}
                                   :body :bs/Tag}
                      :handler (tag-handler/update-tag! db)}

                :delete {:summary "Delete a tag"
                         :parameters {:path {:id pos-int?}}
                         :handler (tag-handler/delete-tag! db)}}]]

      ["/articles" {:swagger {:tags ["Articles"]}}

       ["" {:get {:summary "Query articles"
                  :parameters {:query :bs/query}
                  :handler (article-handler/query-articles db)}

            :post {:summary "New a article"
                   :parameters {:body :bs/Article}
                   :handler (article-handler/create-article! db)}}]

       ["/:id"

        ["" {:get {:summary "Get a article"
                   :parameters {:path {:id string?}}
                   :handler (article-handler/get-article db)}

             :patch {:summary "Update a article"
                     :parameters {:path {:id string?}
                                  :body :bs/Article}
                     :handler (article-handler/update-article! db)}

             :delete {:summary "Delete a article"
                      :parameters {:path {:id string?}}
                      :handler (article-handler/delete-article! db)}}]

        ["/push" {:patch {:summary "Push the article"
                          :parameters {:path {:id string?}
                                       :body :bs/Article-Push}
                          :handler (article-handler/push! db)}}]]]

      ["/articles-comments" {:swagger {:tags ["Articles Comments"]}}

       ["" {:get {:summary "Query all comments"
                  :parameters {:query :bs/query}
                  :handler (article-comment-handler/query-articles-comments db)}}]

       ["/:id" {:get {:summary "Get a comment"
                      :parameters {:path {:id pos-int?}}
                      :handler (article-comment-handler/get-articles-comments-by-id db)}

                :delete {:summary "Delete a comment"
                         :parameters {:path {:id pos-int?}}
                         :handler (article-comment-handler/delete-article-comment! db)}}]]

      ["/app-categories" {:swagger {:tags ["App Categories"]}}

       ["" {:get {:summary "query app categories"
                  :parameters {:query :bs/query}
                  :handler (app-handler/query-app-categories db)}

            :post {:summary "add a app category"
                   :parameters {:body :bs/App-Category}
                   :handler (app-handler/create-app-category! db)}}]

       ["/:id"

        ["" {:get {:summary "Get a App Category"
                   :parameters {:path {:id string?}}
                   :handler (app-handler/get-app-category-by-id db)}

             :delete {:summary "Delete a App Category"
                      :parameters {:path {:id string?}}
                      :handler (app-handler/delete-app-category-by-id! db)}}]]]

      ["/apps" {:swagger {:tags  ["App"]}}

       ["" {:post {:summary "reg a app"
                   :parameters {:body :bs/App}
                   :handler (app-handler/create-app! db)}

            :get {:summary "query apps"
                  :parameters {:query :bs/query}
                  :handler (app-handler/query-apps db)}}]

       ["/:id"

        ["" {:get {:summary "get a App"
                   :parameters {:path  {:id string?}}
                   :handler (app-handler/get-app-by-id db)}

             :delete {:summary "delete a App"
                      :parameters {:path {:id string?}}
                      :handler (app-handler/delete-app-by-id! db)}}]]]

      ["/app-access-logs" {:swagger {:tags ["App Access Log"]}}

       ["" {:get {:summary "get app access logs"
                  :parameters {:query :bs/query}
                  :handler (app-handler/query-app-access-logs db)}}]]

      ["/files"
       {:swagger {:tags ["files"]}}

       ["/upload" {:post {:summary "upload a file"
                          :parameters {:multipart {:file reitit-multipart/temp-file-part}}
                          :handler (fn [{{{:keys [file]} :multipart} :parameters}]
                                     {:status 200
                                      :body {:file file}})}}]

       ["/download" {:get {:summary "downloads a file"
                           :swagger {:produces ["image/png"]}
                           :parameters {}
                           :handler (fn [_]
                                      {:status 200
                                       :headers {"Content-Type" "image/png"}
                                       :body (-> "reitit.png"
                                                 (io/resource)
                                                 (io/input-stream))})}}]]]]))
