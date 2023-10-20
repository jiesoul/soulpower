(ns admin.tag.events
  (:require [admin.http :as f-http]
            [admin.util :as f-util]
            [clojure.string :as str]
            [re-frame.core :as re-frame]))

(re-frame/reg-event-db
 :init-tag
 (fn [db _]
   (dissoc db :tag)))

(defn gen-query-str [{:keys [filter page page-size sort]}]
  (let [qs (str "?page=" page "&page-size=" page-size)
        qs (if filter (str "&filter=" (str/join " and " (vals filter))) qs)
        qs (if sort (str "&filter=" (str/join "," (vals sort))) qs)]
    qs))

(re-frame/reg-event-db
 :query-tags-ok
 (fn [db [_ resp]]
   (assoc-in db [:tag] resp)))

(re-frame/reg-event-fx
 :query-tags
 (fn [{:keys [db]} [_ data]]
   (let [query-str (gen-query-str data)]
     (f-http/http-get (assoc-in db [:tag :query] data)
                      (f-http/api-uri-admin "tags")
                      data
                      [:query-tags-ok]))))

(re-frame/reg-event-fx
 :add-tag-ok
 (fn [{:keys [db]} [_ resp]]
   (let [_ (f-util/clog "add tag ok: " resp)]
     {:db db
      :fx [[:dispatch [:push-toast {:content "添加成功" :type :info}]]
           [:dispatch [:set-modal nil]]]})))

(re-frame/reg-event-fx
 :add-tag
 (fn [{:keys [db]} [_ tag]]
   (f-http/http-post db
                     (f-http/api-uri "admin" "tags")
                     tag
                     [:add-tag-ok])))

(re-frame/reg-event-db
 :set-tag-edit
 (fn [db [_ tag]]
   (assoc-in db [:tag :edit] tag)))

(re-frame/reg-event-fx
 :get-tag-ok
 (fn [{:keys [db]} [_ fx resp]]
   {:db db
    :fx (concat [[:dispatch [:set-tag-edit resp]]] fx)}))

(re-frame/reg-event-fx
 :get-tag
 (fn [{:keys [db]} [_ id fx]]
   (f-http/http-get db
                    (f-http/api-uri-admin "tags" id)
                    {}
                    [:get-tag-ok fx])))

(re-frame/reg-event-fx
 :update-tag-ok
 (fn [{:keys [db]} _]
   {:db db
    :fx [[:dispatch [:push-toast {:content "tag update success"
                                  :type :success}]]]}))

(re-frame/reg-event-fx
 :update-tag
 (fn [{:keys [db]} [_ tag]]
   (f-http/http-put db
                    (f-http/api-uri-admin "tags" (:id tag))
                    tag
                    [:update-tag-ok])))

(re-frame/reg-event-fx
 :delete-tag-ok
 (fn [{:keys [db]} _]
   {:db db
    :fx [[:dispatch [:push-toast {:type :success :content (str "Delete tag success")}]]
         [:dispatch [:set-modal nil]]
         [:dispatch [:set-tag-edit nil]]
         [:dispatch [:query-tags]]]}))

(re-frame/reg-event-fx
 :delete-tag
 (fn [{:keys [db]} [_ id]]
   (f-http/http-delete db
                       (f-http/api-uri-admin "tags" id)
                       {}
                       [:delete-tag-ok])))
