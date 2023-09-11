(ns admin.article-comment.events
  (:require [re-frame.core :as re-frame]
            [admin.util :as f-util]
            [admin.http :as f-http]
            [clojure.string :as str]))

(re-frame/reg-event-db
 :init-article-comment
 (fn [db _]
   (dissoc db :article-comment)))

(defn gen-query-str [{:keys [filter page page-size sort]}]
  (let [qs (str "?page=" page "&page-size=" page-size)
        qs (if filter (str "&filter=" (str/join " and " (vals filter))) qs)
        qs (if sort (str "&filter=" (str/join "," (vals sort))) qs)]
    qs))

(re-frame/reg-event-db
 :query-articles-comments-ok
 (fn [db [_ resp]]
   (assoc-in db [:article-comment :data] (:data resp))))

(re-frame/reg-event-fx
 :query-articles-comments
 (fn [{:keys [db]} [_ data]]
   (let [query-str (gen-query-str data)]
     (f-http/http-get (assoc-in db [:article-comment :query] data)
                      (f-http/api-uri-admin "articles-comments")
                      data
                      [:query-articles-comments-ok]))))

(re-frame/reg-event-fx
 :add-article-comment-ok
 (fn [{:keys [db]} [_ resp]]
   (let [_ (f-util/clog "add article-comment ok: " resp)]
     {:db db
      :fx [[:dispatch [:push-toast {:content "添加成功" :type :info}]]
           [:dispatch [:set-modal nil]]]})))

(re-frame/reg-event-fx
 :add-article-comment
 (fn [{:keys [db]} [_ article-comment]]
   (f-http/http-post db
                     (f-http/api-uri "admin" "articles-comments")
                     {:article-comment article-comment}
                     [:add-article-comment-ok])))

(re-frame/reg-event-db
 :set-article-comment-edit
 (fn [db [_ article-comment]]
   (assoc-in db [:article-comment :edit] article-comment)))

(re-frame/reg-event-fx
 :get-article-comment-ok
 (fn [{:keys [db]} [_ fx resp]]
   {:db db
    :fx (concat [[:dispatch [:set-article-comment-edit (:data resp)]]] fx)}))

(re-frame/reg-event-fx
 :get-article-comment
 (fn [{:keys [db]} [_ id fx]]
   (f-http/http-get db
                    (f-http/api-uri-admin "articles-comments" id)
                    {}
                    [:get-article-comment-ok fx])))

(re-frame/reg-event-fx
 :update-article-comment-ok
 (fn [{:keys [db]} _]
   {:db db
    :fx [[:dispatch [:push-toast {:content "article-comment update success"
                                  :type :success}]]
         [:dispatch [:query-articles-comments]]]}))

(re-frame/reg-event-fx
 :update-article-comment
 (fn [{:keys [db]} [_ article-comment]]
   (f-http/http-patch db
                      (f-http/api-uri-admin "articles-comments" (:id article-comment))
                      {:article-comment article-comment}
                      [:update-article-comment-ok])))

(re-frame/reg-event-fx
 :delete-article-comment-ok
 (fn [{:keys [db]} _]
   {:db db
    :fx [[:dispatch [:push-toast {:type :success :content (str "Delete article-comment success")}]]
         [:dispatch [:set-modal nil]]
         [:dispathc [:set-article-comment-edit nil]]
         [:dispatch [:query-articles-comments]]]}))

(re-frame/reg-event-fx
 :delete-article-comment
 (fn [{:keys [db]} [_ id]]
   (f-http/http-delete db
                       (f-http/api-uri-admin "articles-comments" id)
                       {}
                       [:delete-article-comment-ok])))