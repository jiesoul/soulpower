(ns backend.server
  (:require [backend.handler.article-handler :as article-handler]
            [backend.handler.category-handler :as category-handler]
            [backend.handler.login-handler :as login-handler]
            [backend.handler.tag-handler :as tag-handler]
            [backend.handler.user-handler :as user-handler]
            [backend.middleware :refer [admin-middleware auth-middleware
                                        create-token-auth-middleware]]
            [backend.util.req-uitl :as req-util]
            [reitit.ring.middleware.multipart :as reitit-multipart]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as s]))


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
(s/def ::content string?)
(s/def ::Article 
  (s/keys :opt-un [::title ::summary ::content]))

(s/def ::category-id pos-int?)
(s/def ::tags-id (s/coll-of pos-int?))
(s/def ::push-time inst?)
(s/def ::Article-Push
  (s/keys :opt-un [::category-id ::tags-id ::push-time]))

(defn routes [{:keys [options] :as env}]
  (let [no-doc (:no-doc options)]
    [["/login" {:no-doc no-doc
                :swagger {:tags ["Login"]}
                :post {:summary "login to the web site"
                       :parameters {:body {:login-user ::login-user}}
                       :handler (fn [req]
                                  (let [login-user (req-util/parse-body req :login-user)]
                                    (login-handler/login-auth env login-user)))}}]

     ["" {:no-doc no-doc
          :middleware [(create-token-auth-middleware options)
                       auth-middleware
                       admin-middleware]
          :parameters {:header {:authorization ::token}}}

      ["/logout"  {:swagger {:tags ["Login"]}
                   :post {:summary "user logout"
                          :handler (fn [req]
                                     (let [login-user (req-util/parse-body req :login-user)]
                                       (login-handler/logout env login-user)))}}]

      ["/users" {:swagger {:tags ["User"]}}

       ["" {:get {:summary "Query users"
                  :parameters {:query ::query}
                  :handler (fn [req]
                             (let [query (req-util/parse-query req)]
                               (user-handler/query-users env query)))}}]

       ["/:id" {:get {:summary "Get a user"
                      :parameters {:path {:id pos-int?}}
                      :handler (fn [req]
                                 (let [id (req-util/parse-path req :id)]
                                   (user-handler/get-user env id)))}

                :patch {:summary "Update user profile"
                        :parameters {:path {:id pos-int?}
                                     :body {:user-profile ::UserProfile}}
                        :handler (fn [req]
                                   (let [id (req-util/parse-path req :id)
                                         user-profile (req-util/parse-body req :user-profile)]
                                     (user-handler/update-user-profile! env id user-profile)))}}]

       ["/:id/password" {:patch {:summary "Update a user passwrod"
                                 :parameters {:path {:id pos-int?}
                                              :body {:update-password ::UpdatePassword}}
                                 :handler (fn [req]
                                            (let [id (req-util/parse-path req :id)
                                                  password (req-util/parse-body req :update-password)]
                                              (user-handler/update-user-password! env id password)))}}]]

      ["/categories" {:swagger {:tags ["Categories"]}}

       ["" {:get {:summary "Query categories"
                  :parameters {:query ::query}
                  :handler (fn [req]
                             (let [query (req-util/parse-query req)]
                               (category-handler/query-categories env query)))}

            :post {:summary "New a category"
                   :parameters {:body {:category ::Category}}
                   :handler (fn [req]
                              (let [category (req-util/parse-body req :category)]
                                (category-handler/create-category! env category)))}}]


       ["/:id" {:get {:summary "Get a category"
                      :parameters {:path   {:id pos-int?}}
                      :handler (fn [req]
                                 (let [id (req-util/parse-path req :id)]
                                   (category-handler/get-category env id)))}

                :patch {:summary "Update a category"
                        :parameters {:path {:id pos-int?}
                                     :body {:category ::Category}}

                        :handler (fn [req]
                                   (let [id (req-util/parse-path req :id)
                                         category (req-util/parse-body req :category)]
                                     (category-handler/update-category! env (assoc category :id id))))}

                :delete {:summary "Delete a category"
                         :parameters {:path {:id pos-int?}}
                         :handler (fn [req]
                                    (let [id (req-util/parse-path req :id)]
                                      (category-handler/delete-category! env id)))}}]]

      ["/tags" {:swagger {:tags ["Tags"]}}

       ["" {:get {:summary "Query tags"
                  :parameters {:query ::query}
                  :handler (fn [req]
                             (let [opt (req-util/parse-query req)]
                               (tag-handler/query-tags env opt)))}

            :post {:summary "New a tag"
                   :parameters {:body {:tag ::Tag}}
                   :handler (fn [req]
                              (let [tag (req-util/parse-body req :tag)]
                                (tag-handler/create-tag! env tag)))}}]


       ["/:id" {:get {:summary "Get a tag"
                      :parameters {:path {:id pos-int?}}
                      :handler (fn [req]
                                 (let [id (req-util/parse-path req :id)]
                                   (tag-handler/get-tag env id)))}

                :put {:summary "Update a tag"
                      :parameters {:path {:id pos-int?}
                                   :body {:tag ::Tag}}
                      :handler (fn [req]
                                 (let [tag (req-util/parse-body req :tag)]
                                   (tag-handler/update-tag! env tag)))}

                :delete {:summary "Delete a tag"
                         :parameters {:path {:id pos-int?}}
                         :handler (fn [req]
                                    (let [id (req-util/parse-path req :id)]
                                      (tag-handler/delete-tag! env id)))}}]]


      ["/articles" {:swagger {:tags ["Articles"]}}

       ["" {:get {:summary "Query articles"
                  :parameters {:query ::query}
                  :handler (fn [req]
                             (let [opt (req-util/parse-query req)]
                               (article-handler/query-articles env opt)))}

            :post {:summary "New a article"
                   :parameters {:body {:article ::Article}}
                   :handler (fn [req]
                              (let [article (req-util/parse-body req :article)]
                                (article-handler/create-article! env article)))}}]


       ["/:id" {:get {:summary "Get a article"
                      :parameters {:path {:id string?}}
                      :handler (fn [req]
                                 (let [id (req-util/parse-path req :id)]
                                   (article-handler/get-article env id)))}

                :patch {:summary "Update a article"
                        :parameters {:path {:id string?}
                                     :body {:article ::Article}}
                        :handler (fn [req]
                                   (let [id (req-util/parse-path req :id)
                                         article (req-util/parse-body req :article)]
                                     (article-handler/update-article! env (assoc article :id id))))}

                :delete {:summary "Delete a article"
                         :parameters {:path {:id string?}}
                         :handler (fn [req]
                                    (let [id (req-util/parse-path req :id)]
                                      (article-handler/delete-article! env id)))}}]

       ["/:id/push" {:conflicting true
                     :patch {:summary "Push the article"
                             :parameters {:path {:id string?}
                                          :body {:article ::Article-Push}}
                             :handler (fn [req]
                                        (let [id (req-util/parse-path req :id)
                                              article (req-util/parse-body req :article)]
                                          (article-handler/push! env (assoc article :id id))))}}]]


      ["/articles-comments" {:swagger {:tags ["Articles Comments"]}}

       ["" {:get {:summary "Query all articles comments"
                  :parameters {:query ::query}
                  :handler (fn [req]
                             (let [query (req-util/parse-query req)]
                               (article-handler/query-articles-comments env query)))}}]

       ["/:id" {:get {:summary "Get a article comment"
                      :parameters {:path {:id pos-int?}}
                      :handler (fn [req]
                                 (let [id (req-util/parse-path req :id)]
                                   (article-handler/get-articles-comments-by-id env id)))}

                :delete {:summary "Delete a article comment"
                         :parameters {:path {:id pos-int?}}
                         :handler (fn [req]
                                    (let [id (req-util/parse-path req :id)]
                                      (article-handler/delete-articles-comments-by-id env id)))}}]]

      ["/files"
       {:swagger {:tags ["files"]}}

       ["/upload" {:post {:summary "upload a file"
                          :parameters {:multipart {:file reitit-multipart/temp-file-part}}
                          :responses {200 {:body {:file reitit-multipart/temp-file-part}}}
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