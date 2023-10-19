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
            [backend.util.req-uitl :as req-util]
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
                  :handler (fn [req]
                             (let [login-user (:body-params req)]
                               (login-handler/login-auth db jwt-opts login-user)))}}]]

     ["" {:no-doc no-doc
          :middleware [(create-token-auth-middleware jwt-opts)
                       auth-middleware
                       admin-middleware]
          :parameters {:header {:authorization :bs/token}}}

      ["/users" {:swagger {:tags ["User"]}}

       ["" {:get {:summary "Query users"
                  :parameters {:query :bs/query}
                  :handler (fn [req]
                             (let [query (-> req req-util/parse-opts)]
                               (user-handler/query-users db query)))}}]

       ["/:id" {:parameters {:path {:id pos-int?}}}

        ["" {:get {:summary "Get a user"
                   :handler (fn [req]
                              (let [id (req-util/parse-path req :id)]
                                (user-handler/get-user db id)))}

             :patch {:summary "Update user profile"
                     :parameters {:body :bs/UserProfile}
                     :handler (fn [req]
                                (let [id (req-util/parse-path req :id)
                                      user-profile (req-util/parse-body req :user-profile)]
                                  (user-handler/update-user-profile! db id user-profile)))}}]

        ["/password"
         ["" {:patch {:summary "Update a user passwrod"
                      :parameters {:body :bs/UpdatePassword}
                      :handler (fn [req]
                                 (let [id (req-util/parse-path req :id)
                                       password (req-util/parse-body req)]
                                   (user-handler/update-user-password! db id password)))}}]]]]

      ["/categories" {:swagger {:tags ["Categories"]}}

       ["" {:get {:summary "Query categories"
                  :parameters {:query :bs/query}
                  :handler (fn [req]
                             (let [query (req-util/parse-opts req)]
                               (category-handler/query-categories db query)))}

            :post {:summary "New a category"
                   :parameters {:body :bs/Category}
                   :handler (fn [req]
                              (let [category (req-util/parse-body req)]
                                (category-handler/create-category! db category)))}}]

       ["/:id" {:get {:summary "Get a category"
                      :parameters {:path   {:id pos-int?}}
                      :handler (fn [req]
                                 (let [id (req-util/parse-path req :id)]
                                   (category-handler/get-category db id)))}

                :patch {:summary "Update a category"
                        :parameters {:path {:id pos-int?}
                                     :body :bs/Category}
                        :handler (fn [req]
                                   (let [id (req-util/parse-path req :id)
                                         category (req-util/parse-body req)]
                                     (category-handler/update-category! db (assoc category :id id))))}

                :delete {:summary "Delete a category"
                         :parameters {:path {:id pos-int?}}
                         :handler (fn [req]
                                    (let [id (req-util/parse-path req :id)]
                                      (category-handler/delete-category! db id)))}}]]

      ["/tags" {:swagger {:tags ["Tags"]}}

       ["" {:get {:summary "Query tags"
                  :parameters {:query :bs/query}
                  :handler (fn [req]
                             (let [opt (req-util/parse-opts req)]
                               (tag-handler/query-tags db opt)))}

            :post {:summary "New a tag"
                   :parameters {:body :bs/Tag}
                   :handler (fn [req]
                              (let [tag (req-util/parse-body req)]
                                (tag-handler/create-tag! db tag)))}}]

       ["/:id" {:get {:summary "Get a tag"
                      :parameters {:path {:id pos-int?}}
                      :handler (fn [req]
                                 (let [id (req-util/parse-path req :id)]
                                   (tag-handler/get-tag db id)))}

                :put {:summary "Update a tag"
                      :parameters {:path {:id pos-int?}
                                   :body :bs/Tag}
                      :handler (fn [req]
                                 (let [tag (req-util/parse-body req)]
                                   (tag-handler/update-tag! db tag)))}

                :delete {:summary "Delete a tag"
                         :parameters {:path {:id pos-int?}}
                         :handler (fn [req]
                                    (let [id (req-util/parse-path req :id)]
                                      (tag-handler/delete-tag! db id)))}}]]

      ["/articles" {:swagger {:tags ["Articles"]}}

       ["" {:get {:summary "Query articles"
                  :parameters {:query :bs/query}
                  :handler (fn [req]
                             (let [opt (req-util/parse-opts req)]
                               (article-handler/query-articles db opt)))}

            :post {:summary "New a article"
                   :parameters {:body :bs/Article}
                   :handler (fn [req]
                              (let [article (req-util/parse-body req :article)]
                                (article-handler/create-article! db article)))}}]

       ["/:id"

        ["" {:get {:summary "Get a article"
                   :parameters {:path {:id string?}}
                   :handler (fn [req]
                              (let [id (req-util/parse-path req :id)]
                                (article-handler/get-article db id)))}

             :patch {:summary "Update a article"
                     :parameters {:path {:id string?}
                                  :body :bs/Article}
                     :handler (fn [req]
                                (let [id (req-util/parse-path req :id)
                                      article (req-util/parse-body req)]
                                  (article-handler/update-article! db id article)))}

             :delete {:summary "Delete a article"
                      :parameters {:path {:id string?}}
                      :handler (fn [req]
                                 (let [id (req-util/parse-path req :id)]
                                   (article-handler/delete-article! db id)))}}]

        ["/push" {:patch {:summary "Push the article"
                          :parameters {:path {:id string?}
                                       :body :bs/Article-Push}
                          :handler (fn [req]
                                     (let [id (req-util/parse-path req :id)
                                           article (req-util/parse-body req)]
                                       (article-handler/push! db id (assoc article :id id))))}}]]]

      ["/articles-comments" {:swagger {:tags ["Articles Comments"]}}

       ["" {:get {:summary "Query all comments"
                  :parameters {:query :bs/query}
                  :handler (fn [req]
                             (let [query (req-util/parse-opts req)]
                               (article-comment-handler/query-articles-comments db query)))}}]

       ["/:id" {:get {:summary "Get a comment"
                      :parameters {:path {:id pos-int?}}
                      :handler (fn [req]
                                 (let [id (req-util/parse-path req :id)]
                                   (article-comment-handler/get-articles-comments-by-id db id)))}

                :delete {:summary "Delete a comment"
                         :parameters {:path {:id pos-int?}}
                         :handler (fn [req]
                                    (let [id (req-util/parse-path req :id)]
                                      (article-comment-handler/delete-article-comment! db id)))}}]]

      ["/app-categories" {:swagger {:tags ["App Categories"]}}

       ["" {:get {:summary "query app categories"
                  :parameters {:query :bs/query}
                  :handler (fn [req]
                             (let [query (req-util/parse-opts req)]
                               (app-handler/query-app-categories db query)))}

            :post {:summary "add a app category"
                   :parameters {:body :bs/App-Category}
                   :handler (fn [req]
                              (let [app-category (req-util/parse-body req)]
                                (app-handler/create-app-category! db app-category)))}}]

       ["/:id"

        ["" {:get {:summary "Get a App Category"
                   :parameters {:path {:id string?}}
                   :handler (fn [req]
                              (let [id (req-util/parse-path req :id)]
                                (app-handler/get-app-category-by-id db id)))}

             :delete {:summary "Delete a App Category"
                      :parameters {:path {:id string?}}
                      :handler (fn [req]
                                 (let [id (req-util/parse-path req :id)]
                                   (app-handler/delete-app-category-by-id! db id)))}}]]]

      ["/apps" {:swagger {:tags  ["App"]}}

       ["" {:post {:summary "reg a app"
                   :parameters {:body :bs/App}
                   :handler (fn [req]
                              (let [app (req-util/parse-body req)]
                                (app-handler/create-app! db app)))}

            :get {:summary "query apps"
                  :parameters {:query :bs/query}
                  :handler (fn [req]
                             (let [query (req-util/parse-opts req)]
                               (app-handler/query-apps db query)))}}]

       ["/:id"

        ["" {:get {:summary "get a App"
                   :parameters {:path  {:id string?}}
                   :handler (fn [req]
                              (let [id (req-util/parse-path req :id)]
                                (app-handler/get-app-by-id db id)))}

             :delete {:summary "delete a App"
                      :parameters {:path {:id string?}}
                      :handler (fn [req]
                                 (let [id (req-util/parse-path req :id)]
                                   (app-handler/delete-app-by-id! db id)))}}]]]

      ["/app-access-logs" {:swagger {:tags ["App Access Log"]}}

       ["" {:get {:summary "get app access logs"
                  :parameters {:query :bs/query}
                  :handler (fn [req]
                             (let [query (req-util/parse-opts req)]
                               (app-handler/query-app-access-logs db query)))}}]]

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
