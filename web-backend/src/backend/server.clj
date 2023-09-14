(ns backend.server
  (:require [backend.handler.article-handler :as article-handler]
            [backend.handler.category-handler :as category-handler]
            [backend.handler.login-handler :as login-handler]
            [backend.handler.tag-handler :as tag-handler]
            [backend.handler.user-handler :as user-handler]
            [backend.middleware :refer [admin-middleware
                                        auth-middleware create-token-auth-middleware]]
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

(s/def ::birth-date inst?)

(s/def ::username string?)
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

(defn routes [{:keys [options] :as env}]
  (let [no-doc (:no-doc options)]
    [["/login" {:no-doc no-doc
                :swagger {:tags ["Login"]}
                :post {:summary "login to the web site"
                       :parameters {:body {:login-user ::login-user}}
                       :handler (fn [req]
                                  (login-handler/login-auth env req))}}]

     ["/admin" {:no-doc no-doc
                :middleware [(create-token-auth-middleware options)
                             auth-middleware
                             admin-middleware]
                :parameters {:header {:authorization ::token}}}

      ["/logout"  {:swagger {:tags ["Login"]}
                   :post {:summary "user logout"
                         :handler (fn [req] 
                                    (login-handler/logout env req))}}]

      ["/users" {:swagger {:tags ["User"]}}
       
       ["" {:get {:summary "Query users"
                  :parameters {:query ::query}
                  :handler (fn [req]
                             (user-handler/query-users env req))}}]

       ["/:id" {:get {:summary "Get a user"
                      :parameters {:path {:id pos-int?}}
                      :handler (fn [req]
                                 (user-handler/get-user env req))}
                
                :patch {:summary "Update user profile"
                        :parameters {:path {:id pos-int?}
                                     :body {:user-profile ::UserProfile}}
                        :handler (fn [req]
                                   (user-handler/update-user-profile! env req))}}]

       ["/:id/password" {:patch {:summary "Update a user passwrod"
                                 :parameters {:path {:id pos-int?}
                                              :body {:update-password ::UpdatePassword}}
                                 :handler (fn [req]
                                              (user-handler/update-user-password! env req))}}]]

      ["/categories" {:swagger {:tags ["Categories"]}}

       ["" {:get {:summary "Query categories"
                  :parameters {:query ::query}
                  :handler (fn [req]
                             (let [query (req-util/parse-query req)]
                               (category-handler/query-categories env query)))}

            :post {:summary "New a category"
                   :parameters {:body {:category ::CategoryAdd}}
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
                                     :body {:category ::CategoryUpdate}}

                        :handler (fn [req]
                                   (let [category (req-util/parse-body req :category)]
                                     (category-handler/update-category! env category)))}

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
                   :parameters {:body {:tag ::TagAdd}}
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
                                   :body {:tag ::TagUpdate}}
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
                   :handler (fn [req]
                              (log/debug  "new a article req: " (:body-params req))
                              (let [article (req-util/parse-body req :article)]
                                (article-handler/create-article! env article)))}}]


       ["/:id" {:get {:summary "Get a article"
                      :parameters {:path {:id string?}}
                      :handler (fn [req]
                                 (let [id (req-util/parse-path req :id)]
                                   (article-handler/get-article env id)))}

                :patch {:summary "Update a article"
                        :parameters {:path {:id string?}}
                        :handler (fn [req]
                                   (let [article (req-util/parse-body req :article)]
                                     (article-handler/update-article! env article)))}

                :delete {:summary "Delete a article"
                         :parameters {:path {:id string?}}
                         :handler (fn [req]
                                    (let [id (req-util/parse-path req :id)]
                                      (article-handler/delete-article! env id)))}}]

       ["/:id/push" {:patch {:summary "Query the comments of a article"
                             :parameters {:path {:id string?}}
                             :handler (fn [req]
                                        (let [article (req-util/parse-body req :article)]
                                          (article-handler/push! env article)))}}]

       ["/:id/comments" {:get {:summary "Query the comments of a article"
                               :parameters {:path {:id string?}}
                               :handler (fn [req]
                                          (let [article-id (req-util/parse-path req :id)]
                                            (article-handler/get-comments-by-article-id env article-id)))}
                         :post {:summary "add a comments of the article"
                                :parameters {:path {:id string?}}
                                :handler (fn [req]
                                           (let [comment (req-util/parse-body req :comment)]
                                             (article-handler/save-comment! env comment)))}}]]


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
                                      (article-handler/delete-articles-comments-by-id env id)))}}]]]]))


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