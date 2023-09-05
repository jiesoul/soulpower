(ns admin.tag.events
  (:require [re-frame.core :as re-frame]
            [admin.util :as f-util]
            [admin.http :as f-http]))

(re-frame/reg-event-fx
 ::query-tags-ok
 (fn [{:keys [db]} [_ resp]]
   {:db db
    :fx [[:dispatch [:init-current-route-result (:data resp)]]]}))

(re-frame/reg-event-fx
 ::query-tags
 (fn [{:keys [db]} [_ data]]
   (f-util/clog "query tags: " data)
   (f-http/http-get db
                    (f-http/api-uri "/admin/tags")
                    data
                    [::query-tags-ok])))

(re-frame/reg-event-fx
 ::new-tag-ok
 (fn [{:keys [db]} [_ tag]]
   {:db db
    :fx [[:dispatch [:push-toast {:content (str  "Tag " (:name tag) " 添加成功") :type :info}]]]}))

(re-frame/reg-event-fx
 ::new-tag
 (fn [{:keys [db]} [_ tag]]
   (f-http/http-post db
                     (f-http/api-uri "/admin/tags")
                     {:tag tag}
                     [::new-tag-ok tag])))

(re-frame/reg-event-fx
 ::get-tag-ok
 (fn [{:keys [db]} [_ resp]]
   {:db db
    :fx [[:dispatch [:init-current-route-edit (:data resp)]]]}))

(re-frame/reg-event-fx
 ::get-tag
 (fn [{:keys [db]} [_ id]]
   (f-util/clog "Get a tag")
   (f-http/http-get db
                    (f-http/api-uri "/admin/tags/" id)
                    {}
                    [::get-tag-ok])))

(re-frame/reg-event-fx
 ::update-tag-ok
 (fn [{:keys [db]} [_ tag]]
   {:db db
    :fx [[:dispatch [:push-toast {:content (str "Tag:" (:name tag)  " 更新成功")
                                    :type :success}]]]}))

(re-frame/reg-event-fx
 ::update-tag
 (fn [{:keys [db]} [_ tag]]
   (f-http/http-put db
                    (f-http/api-uri "/admin/tags/" (:id tag))
                    {:tag tag}
                    [::update-tag-ok tag])))

(re-frame/reg-event-fx
 ::delete-tag-ok
 (fn [{:keys [db]} [_ {:keys [id name]}]]
   (let [tags (remove #(= id (:id %)) (-> db :current-route :result :list))]
     {:db (assoc-in db [:current-route :result :list] tags)
      :fx [[:dispatch [:push-toast {:type :success :content (str "Tag: " name " Delete success")}]]
           [:dispatch [:clean-current-route-edit]]
           [:dispatch [:close-modal :delete-modal?]]]})))

(re-frame/reg-event-fx
 ::delete-tag
 (fn [{:keys [db]} [_ {:keys [id] :as t}]]
   (f-http/http-delete db
                       (f-http/api-uri "/admin/tags/" id)
                       {}
                       [::delete-tag-ok t])))