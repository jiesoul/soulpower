(ns backend.server
  (:require [backend.handler.app-handler :as app-handler]
            [backend.handler.article-handler :as article-handler]
            [backend.handler.category-handler :as category-handler]
            [backend.handler.comment-handler :as article-comment-handler]
            [backend.handler.login-handler :as login-handler]
            [backend.handler.tag-handler :as tag-handler]
            [backend.handler.user-handler :as user-handler]
            [backend.middleware :refer [admin-middleware auth-middleware
                                        create-token-auth-middleware]]
            [backend.util.req-uitl :as req-util]
            [backend.spec :as bs]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [reitit.ring.middleware.multipart :as reitit-multipart]))


(s/def ::not-empty-string (s/and string? #(> (count %) 0)))
(s/def ::password-type (s/and string? #(>= (count %) 8)))

(s/def ::token (s/and string? #(re-matches #"^Token (.+)$" %)))
(s/def ::name ::not-empty-string)
(s/def ::description string?)

(s/def ::page pos-int?)
(s/def ::page-size pos-int?)
(s/def ::sort string?)
(s/def ::filter string?)
(s/def ::q string?)
(s/def ::query
  (s/keys :opt-un [::page ::page-size ::sort ::filter ::q]))

(s/def ::birth-date inst?)

(s/def ::username ::not-empty-string)
(s/def ::nickname string?)
(s/def ::birthday ::birth-date)
(s/def ::password ::password-type)
(s/def ::age pos-int?)
(s/def ::avatar string?)
(s/def ::phone string?)

(s/def ::login-user
  (s/keys :req-un [::username ::password]))

(s/def ::UserProfile
  (s/keys :opt-un [::nickname ::birthday ::age ::avatar ::phone]))

(s/def ::old-password ::password-type)
(s/def ::new-password ::password-type)
(s/def ::confirm-password ::password-type)
(s/def ::UpdatePassword
  (s/keys :req-un [::old-password ::new-password ::confirm-password]))

(s/def ::Category
  (s/keys :req-un [::name]
          :opt-un [::description]))

(s/def ::Tag
  (s/keys :req-un [::name]
          :opt-un [::description]))

(s/def ::title string?)
(s/def ::summary string?)
(s/def ::content-md string?)
(s/def ::author string?)
(s/def ::detail 
  (s/keys :req-un [::content-md]))
(s/def ::Article 
  (s/keys :opt-un [::title ::summary ::author ::detail]))

(s/def ::category-id pos-int?)
(s/def ::tag-ids (s/coll-of pos-int?))
(s/def ::Article-Push
  (s/keys :req-un [::category-id ::tag-ids]))

(s/def ::secret string?)
(s/def ::app-category-id string?)
(s/def ::App 
  (s/keys :req-un [::name ::secret ::app-category-id] :opt-un [::description]))

(s/def ::id string?)
(s/def ::App-Category
  (s/keys :req-un [::id ::name] :opt-un [::description]))

(defn routes [{:keys [options db]}]
  (let [{:keys [jwt-opts no-doc]} options
        no-doc (or no-doc false)]
    [["/login" {:no-doc no-doc
                :swagger {:tags ["Login"]}}
      
      ["" {:post {:summary "login to the web site"
                  :parameters {:body {:login-user :bs/login-user}}
                  :handler (fn [req]
                             (let [login-user (req-util/parse-body req :login-user)]
                               (login-handler/login-auth db jwt-opts login-user)))}}]]

     ["" {:no-doc no-doc
          :middleware [(create-token-auth-middleware jwt-opts)
                       auth-middleware
                       admin-middleware]
          :parameters {:header {:authorization ::token}}}

      ["/users" {:swagger {:tags ["User"]}}

       ["" {:get {:summary "Query users"
                  :parameters {:query ::query}
                  :handler (fn [req]
                             (let [query (req-util/parse-query req)]
                               (user-handler/query-users db query)))}}]

       ["/:id" {:parameters {:path {:id pos-int?}}}
        ["" {:get {:summary "Get a user"
                   :handler (fn [req]
                              (let [id (req-util/parse-path req :id)]
                                (user-handler/get-user db id)))}

             :patch {:summary "Update user profile"
                     :parameters {:body {:user-profile ::UserProfile}}
                     :handler (fn [req]
                                (let [id (req-util/parse-path req :id)
                                      user-profile (req-util/parse-body req :user-profile)]
                                  (user-handler/update-user-profile! db id user-profile)))}}]
        
        ["/password" 
         ["" {:patch {:summary "Update a user passwrod"
                      :parameters {
                                   :body {:update-password ::UpdatePassword}}
                      :handler (fn [req]
                                 (let [id (req-util/parse-path req :id)
                                       password (req-util/parse-body req :update-password)]
                                   (user-handler/update-user-password! db id password)))}}]]]]

      ["/categories" {:swagger {:tags ["Categories"]}}

       ["" {:get {:summary "Query categories"
                  :parameters {:query ::query}
                  :handler (fn [req]
                             (let [query (req-util/parse-query req)]
                               (category-handler/query-categories db query)))}

            :post {:summary "New a category"
                   :parameters {:body {:category ::Category}}
                   :handler (fn [req]
                              (let [category (req-util/parse-body req :category)]
                                (category-handler/create-category! db category)))}}]


       ["/:id" {:get {:summary "Get a category"
                      :parameters {:path   {:id pos-int?}}
                      :handler (fn [req]
                                 (let [id (req-util/parse-path req :id)]
                                   (category-handler/get-category db id)))}

                :patch {:summary "Update a category"
                        :parameters {:path {:id pos-int?}
                                     :body {:category ::Category}}

                        :handler (fn [req]
                                   (let [id (req-util/parse-path req :id)
                                         category (req-util/parse-body req :category)]
                                     (category-handler/update-category! db (assoc category :id id))))}

                :delete {:summary "Delete a category"
                         :parameters {:path {:id pos-int?}}
                         :handler (fn [req]
                                    (let [id (req-util/parse-path req :id)]
                                      (category-handler/delete-category! db id)))}}]]

      ["/tags" {:swagger {:tags ["Tags"]}}

       ["" {:get {:summary "Query tags"
                  :parameters {:query ::query}
                  :handler (fn [req]
                             (let [opt (req-util/parse-query req)]
                               (tag-handler/query-tags db opt)))}

            :post {:summary "New a tag"
                   :parameters {:body {:tag ::Tag}}
                   :handler (fn [req]
                              (let [tag (req-util/parse-body req :tag)]
                                (tag-handler/create-tag! db tag)))}}]


       ["/:id" {:get {:summary "Get a tag"
                      :parameters {:path {:id pos-int?}}
                      :handler (fn [req]
                                 (let [id (req-util/parse-path req :id)]
                                   (tag-handler/get-tag db id)))}

                :put {:summary "Update a tag"
                      :parameters {:path {:id pos-int?}
                                   :body {:tag ::Tag}}
                      :handler (fn [req]
                                 (let [tag (req-util/parse-body req :tag)]
                                   (tag-handler/update-tag! db tag)))}

                :delete {:summary "Delete a tag"
                         :parameters {:path {:id pos-int?}}
                         :handler (fn [req]
                                    (let [id (req-util/parse-path req :id)]
                                      (tag-handler/delete-tag! db id)))}}]]


      ["/articles" {:swagger {:tags ["Articles"]}}

       ["" {:get {:summary "Query articles"
                  :parameters {:query ::query}
                  :handler (fn [req]
                             (let [opt (req-util/parse-query req)]
                               (article-handler/query-articles db opt)))}

            :post {:summary "New a article"
                   :parameters {:body {:article ::Article}}
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
                                  :body {:article ::Article}}
                     :handler (fn [req]
                                (let [id (req-util/parse-path req :id)
                                      article (req-util/parse-body req :article)]
                                  (article-handler/update-article! db id article)))}

             :delete {:summary "Delete a article"
                      :parameters {:path {:id string?}}
                      :handler (fn [req]
                                 (let [id (req-util/parse-path req :id)]
                                   (article-handler/delete-article! db id)))}}]


        ["/push" {:patch {:summary "Push the article"
                          :parameters {:path {:id string?}
                                       :body {:article ::Article-Push}}
                          :handler (fn [req]
                                     (let [id (req-util/parse-path req :id)
                                           article (req-util/parse-body req :article)]
                                       (article-handler/push! db id (assoc article :id id))))}}]]]

      ["/articles-comments" {:swagger {:tags ["Articles Comments"]}}

       ["" {:get {:summary "Query all comments"
                  :parameters {:query ::query}
                  :handler (fn [req]
                             (let [query (req-util/parse-query req)]
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
      
      ["/apps" {:swagger {:tags  ["App"]}}
       ["" {:post {:summary "reg a app"
                   :parameters {:body {:app ::App}}
                   :handler (fn [req]
                              (let [app (req-util/parse-body req :app)]
                                (app-handler/create-app! db app)))}
            
            :get {:summary "query apps"
                  :parameters {:query ::query}
                  :handler (fn [req]
                             (let [query (req-util/parse-query req)]
                               (app-handler/query-apps db query)))}}]
       
       ["/:id" {:parameters {:path int?}}
        ["" {:get {:summary "get a App"
                   :handler (fn [req] )}}]]]
      
      ["/app-categories" {:swagger {:tags ["App Categories"]}}
       ["" {:get {:summary "get app categories"
                  :parameters {:query ::query}
                  :handler (fn [req]
                             (let [query (req-util/parse-query req)]
                               (app-handler/query-app-categories db query)))}
            :post {:summary "add a app category"
                   :parameters {:body {:app-category ::App-Category}}
                   :handler (fn [req]
                              (let [app-category (req-util/parse-body req :app-category)]
                                (app-handler/create-app-category! db app-category)))}}]
       
       ["/:id" {:parameters {:path int?}} 
        ["" {:get {:summary "Get a comment"
                   :parameters {:path {:id pos-int?}}
                   :handler (fn [req]
                              (let [id (req-util/parse-path req :id)]
                                (article-comment-handler/get-articles-comments-by-id db id)))}

             :delete {:summary "Delete a comment"
                      :parameters {:path {:id pos-int?}}
                      :handler (fn [req]
                                 (let [id (req-util/parse-path req :id)]
                                   (article-comment-handler/delete-article-comment! db id)))}}]]]
      
      ["/app-access-logs" {:swagger {:tags ["App Access Log"]}}
       ["" {:get {:summary "get app access logs"
                  :parameters {:query ::query}
                  :handler (fn [req]
                             (let [query (req-util/parse-query req)]
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