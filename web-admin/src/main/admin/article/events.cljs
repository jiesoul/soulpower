(ns admin.article.events
  (:require [re-frame.core :as re-frame :refer [reg-event-db reg-event-fx]]
            [admin.util :as util]
            [admin.http :as http]
            [admin.subs]))

(re-frame/reg-event-db
 ::get-view-article-ok
 (fn [db [_ resp]]
   (util/clog "get view article ok: " resp)
   (assoc-in db [:current-route :result] (:data resp))))

(re-frame/reg-event-fx
 ::get-view-article
 (fn [{:keys [db]} [_ id]]
   (util/clog "Get view article: " id)
   (http/http-get db
                    (http/api-uri "/articles/" id)
                    {}
                    [::get-view-article-ok])))

(re-frame/reg-event-fx
 ::query-articles-ok
 (fn [{:keys [db]} [_ resp]]
   {:db db
    :fx [[:dispatch [:init-current-route-result (:data resp)]]]}))

(re-frame/reg-event-fx
 ::query-articles
 (fn [{:keys [db]} [_ data]]
   (util/clog "query articles: " data)
   (http/http-get db
                    (http/api-uri "/admin/articles")
                    data
                    [::query-articles-ok])))

(re-frame/reg-event-fx
 ::add-article-ok
 (fn [{:keys [db]} [_ resp]]
   (util/clog "add article ok: " resp)
   {:db db
    :fx [[:dispatch [:push-toast {:content "保存成功" :type :info}]]]}))

(re-frame/reg-event-fx
 ::add-article
 (fn [{:keys [db]} [_ article]]
   (util/clog "add article: " article)
   (http/http-post db
                     (http/api-uri "/admin/articles")
                     {:article article}
                     [::add-article-ok])))

(re-frame/reg-event-db
 ::get-article-ok
 (fn [db [_ resp]]
   (util/clog "get article ok: " resp)
   (assoc-in db [:current-route :edit] (:data resp))))

(re-frame/reg-event-fx
 ::get-article
 (fn [{:keys [db]} [_ id]]
   (util/clog "Get a article: " id)
   (http/http-get db
                    (http/api-uri "/admin/articles/" id)
                    {}
                    [::get-article-ok])))

(re-frame/reg-event-fx
 ::update-article-ok
 (fn [{:keys [db]} [_ resp]]
   (util/clog "update article ok: " resp)
   {:db db
    :fx [[:dispatch [:push-toast {:content "保存成功" :type :success}]]]}))

(re-frame/reg-event-fx
 ::update-article
 (fn [{:keys [db]} [_ article]]
   (util/clog "update article: " article)
   (http/http-patch db
                      (http/api-uri "/admin/articles/" (:id article))
                      {:article article}
                      [::update-article-ok])))


(re-frame/reg-event-fx
 ::push-article-ok
 (fn [{:keys [db]} [_ resp]]
   (util/clog "push article ok: " resp)
   {:db db
    :fx [[:dispatch [:push-toast {:content "Push Success!!!" :type :success}]]]}))

(re-frame/reg-event-fx
 ::push-article
 (fn [{:keys [db]} [_ article]]
   (util/clog "push article: " article)
   (http/http-patch db
                      (http/api-uri "/admin/articles/" (:id article) "/push")
                      {:article article}
                      [::push-article-ok])))

(re-frame/reg-event-fx
 ::delete-article-ok
 (fn [{:keys [db]} [_ resp]]
   (util/clog "delete article ok: " resp)
   {:db db
    :fx [[:dispatch [:push-toast {:type :success
                                    :content "Delete success"}]]
         [:dispatch [:clean-current-route-edit]]
         [:dispatch [:show-delete-modal false]]]}))

(re-frame/reg-event-fx
 ::delete-article
 (fn [{:keys [db]} [_ id]]
   (util/clog "Delete article")
   (http/http-delete db
                       (http/api-uri "/admin/articles/" id)
                       {}
                       [::delete-article-ok])))

;; -- GET Articles @ /api/articles --------------------------------------------
;;
(reg-event-fx                                            ;; usage (dispatch [:get-articles {:limit 10 :tag "tag-name" ...}])
 :get-articles                                           ;; triggered every time user request articles with different id
 (fn [{:keys [db]} [_ id]]  
   (http/http-get (-> db
                      (assoc-in [:loading :articles] true)
                      (assoc-in [:filter :offset] (:offset id)) 
                      (assoc-in [:filter :tag] (:tag id)) 
                      (assoc-in [:filter :author] (:author id)) 
                      (assoc-in [:filter :favorites] (:favorited id))
                      (assoc-in [:filter :feed] false))
                  (http/api-uri "admin/articles")
                  id
                  [:get-articles-success]
                  [:api-request-error {:request-type :get-articles
                                       :loading :articles}]))) 

(reg-event-db
 :get-articles-success
 (fn [db [_ {articles :articles, articles-count :articlesCount}]]
   (-> db
       (assoc-in [:loading :articles] false)          
       (assoc :articles-count articles-count          
              :articles articles))))

;; -- GET Article @ /api/articles/:slug ---------------------------------------
;;
(reg-event-fx                                         
 :get-article                                        
 (fn [{:keys [db]} [_ id]] 
   (http/http-get (assoc-in db [:laoding :article] true)
                  (http/api-uri "admin/articles/" id)
                  [:get-article-success]
                  [:api-request-error {:request-type :get-article 
                                       :loading :article}])))

(reg-event-db
 :get-article-success
 (fn [db [_ {article :article}]]
   (-> db
       (assoc-in [:loading :article] false)
       (assoc :articles article))))

;; -- POST/PUT Article @ /api/articles(/:slug) --------------------------------
;;
;; (reg-event-fx                                             
;;  :upsert-article                                         
;;  (fn [{:keys [db]} [_ id]]                            
;;    {:db         (assoc-in db [:loading :article] true)
;;     :http-xhrio {:method          (if (:slug id) :put :post) ;; when we get a slug we'll update (:put) otherwise insert (:post)
;;                  :uri             (if (:slug id)     ;; Same logic as above but we go with different endpoint -
;;                                     (endpoint "articles" (:slug id)) ;; one with :slug to update
;;                                     (endpoint "articles")) ;; and another to insert
;;                  :headers         (auth-header db)       ;; get and pass user token obtained during login
;;                  :id          {:article (:article id)}
;;                  :format          (json-request-format)  ;; make sure we are doing request format wiht json
;;                  :response-format (json-response-format {:keywords? true}) ;; json response and all keys to keywords
;;                  :on-success      [:upsert-article-success] ;; trigger :upsert-article-success event
;;                  :on-failure      [:api-request-error {:request-type :upsert-article ;; trigger :api-request-error event with request type :upsert-article
;;                                                        :loading :article}]}}))

;; (reg-event-fx
;;  :upsert-article-success
;;  (fn [{:keys [db]} [_ {article :article}]]
;;    {:db         (-> db
;;                     (assoc-in [:loading :article] false)
;;                     (dissoc :comments)                   ;; clean up any comments that we might have in db
;;                     (dissoc :errors)                     ;; clean up any errors that we might have in db
;;                     (assoc :active-page :article
;;                            :active-article (:slug article)))
;;     :dispatch-n [[:get-article {:slug (:slug article)}]           ;; when the users clicks save we fetch the new version
;;                  [:get-article-comments {:slug (:slug article)}]] ;; of the article and comments from the server
;;     :set-url    {:url (str "/article/" (:slug article))}}))

;; -- DELETE Article @ /api/articles/:slug ------------------------------------
;;
;; (reg-event-fx                                              ;; usage (dispatch [:delete-article slug])
;;  :delete-article                                           ;; triggered when a user deletes an article
;;  (fn [{:keys [db]} [_ slug]]                               ;; slug = {:slug "article-slug"}
;;    {:db         (assoc-in db [:loading :article] true)
;;     :http-xhrio {:method          :delete
;;                  :uri             (endpoint "articles" slug) ;; evaluates to "api/articles/:slug"
;;                  :headers         (auth-header db)       ;; get and pass user token obtained during login
;;                  :id          slug                   ;; pass the article slug to delete
;;                  :format          (json-request-format)  ;; make sure we are doing request format wiht json
;;                  :response-format (json-response-format {:keywords? true}) ;; json response and all keys to keywords
;;                  :on-success      [:delete-article-success] ;; trigger :delete-article-success event
;;                  :on-failure      [:api-request-error {:request-type :delete-article ;; trigger :api-request-error event with request type :delete-article
;;                                                        :loading :article}]}}))

;; (reg-event-fx
;;  :delete-article-success
;;  (fn [{:keys [db]} _]
;;    {:db       (-> db
;;                   (update :articles dissoc (:active-article db))
;;                   (assoc-in [:loading :article] false))
;;     :dispatch [:set-active-page {:page :home}]}))

;; ;; -- GET Feed Articles @ /api/articles/feed ----------------------------------
;; ;;
;; (reg-event-fx                                              ;; usage (dispatch [:get-feed-articles {:limit 10 :offset 0 ...}])
;;  :get-feed-articles                                        ;; triggered when Your Feed tab is loaded
;;  (fn [{:keys [db]} [_ id]]                             ;; id = {:offset 0 :limit 10}
;;    {:http-xhrio {:method          :get
;;                  :uri             (endpoint "articles" "feed") ;; evaluates to "api/articles/feed"
;;                  :id          id                 ;; include id in the request
;;                  :headers         (auth-header db)       ;; get and pass user token obtained during login
;;                  :response-format (json-response-format {:keywords? true}) ;; json response and all keys to keywords
;;                  :on-success      [:get-feed-articles-success] ;; trigger :get-feed-articles-success event
;;                  :on-failure      [:api-request-error {:request-type :get-feed-articles ;; trigger :api-request-error event with request type :get-feed-articles
;;                                                        :loading :articles}]}
;;     :db         (-> db
;;                     (assoc-in [:loading :articles] true)
;;                     (assoc-in [:filter :offset] (:offset id))
;;                     (assoc-in [:filter :tag] nil)        ;; with feed-articles, we turn off almost all
;;                     (assoc-in [:filter :author] nil)     ;; filters to make sure everything on the
;;                     (assoc-in [:filter :favorites] nil)  ;; client is displayed correctly.
;;                     (assoc-in [:filter :feed] true))}))  ;; This is the only one we need

;; (reg-event-db
;;  :get-feed-articles-success
;;  (fn [db [_ {articles :articles, articles-count :articlesCount}]]
;;    (-> db
;;        (assoc-in [:loading :articles] false)
;;        (assoc :articles-count articles-count
;;               :articles (index-by :slug articles)))))
