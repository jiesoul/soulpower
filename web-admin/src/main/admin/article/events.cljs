(ns admin.article.events
  (:require [re-frame.core :as re-frame]
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