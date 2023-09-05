(ns admin.article-comment.events
  (:require [re-frame.core :as re-frame]
            [admin.util :as f-util]
            [admin.http :as f-http]))

(re-frame/reg-event-db
 ::query-articles-comments-ok
 (fn [{:keys [db]} [_ resp]]
   {:db db
    :fx [[:dispatch [:init-current-route-result (:data resp)]]]}))

(re-frame/reg-event-fx
 ::query-articles-comments
 (fn [{:keys [db]} [_ data]]
   (f-util/clog "query articles-comments: " data)
   (f-http/http-get db
                    (f-http/api-uri "/admin/articles-comments")
                    data
                    [::query-articles-comments-ok])))

(re-frame/reg-event-fx
 ::add-article-comments-ok
 (fn [{:keys [db]} [_ resp]]
   (f-util/clog "add article-comments ok: " resp)
   {:db (-> db
            (update-in [:toasts] conj {:content "添加成功" :type :info}))}))

(re-frame/reg-event-fx
 ::add-article-comments
 (fn [{:keys [db]} [_ article-comments]]
   (f-util/clog "add article-comments: " article-comments)
   (f-http/http-post db
                     (f-http/api-uri "/article-commentss-comments")
                     {:article-comments article-comments}
                     [::add-article-comments-ok])))

(re-frame/reg-event-db
 ::get-article-comments-ok
 (fn [db [_ resp]]
   (assoc-in db [:article-comments :current] (:article-comments (:data resp)))))

(re-frame/reg-event-fx
 ::get-article-comments
 (fn [{:keys [db]} [_ id]]
   (f-util/clog "Get a article-comments")
   (f-http/http-get db
                    (f-http/api-uri "/admin/article-commentss-comments/" id)
                    {}
                    [::get-article-comments-ok])))

(re-frame/reg-sub
 ::article-comments-current
 (fn [db]
   (get-in db [:article-comments :current])))

(re-frame/reg-event-fx
 ::delete-article-comments-ok
 (fn [{:keys [db]} [_ resp]]
   (f-util/clog "delete article-comments ok: " resp)
   {:db db
    :fx [[:dispatch [:push-toast {:type :success
                                    :content "Delete success"}]]
         [:dispatch [::clean-current]]
         [:dispatch [::show-delete-modal false]]]}))

(re-frame/reg-event-fx
 ::delete-article-comments
 (fn [{:keys [db]} [_ id]]
   (f-util/clog "Delete article-comments")
   (f-http/http-delete db
                       (f-http/api-uri "/admin/article-commentss-comments/" id)
                       {}
                       [::delete-article-comments-ok])))