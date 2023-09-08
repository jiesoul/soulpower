(ns admin.category.events
  (:require [re-frame.core :as re-frame]
            [admin.util :as f-util]
            [admin.http :as f-http]))

(re-frame/reg-event-db
 :query-categories-ok
 (fn [db [_ resp]]
   (assoc-in db [:category] (:data resp))))

(re-frame/reg-event-fx
 :query-categories
 (fn [{:keys [db]} [_ data]]
   (let [default-query (get-in db [:default-query])
         query (merge default-query data)]
   (f-http/http-get (assoc-in db [:category :query] query)
                    (f-http/api-uri-admin "categories")
                    query
                    [:query-categories-ok]))))

(re-frame/reg-event-fx
 :add-category-ok
 (fn [{:keys [db]} [_ resp]]
   (let [_ (f-util/clog "add category ok: " resp)]
     {:db db
      :fx [[:dispatch [:push-toast {:content "添加成功" :type :info}]]]})))

(re-frame/reg-event-fx
 :add-category
 (fn [{:keys [db]} [_ category]]
   (f-http/http-post db
                     (f-http/api-uri "admin" "categories")
                     {:category category}
                     [:add-category-ok])))

(re-frame/reg-event-db 
 :set-category-edit
 (fn [db [_ category]]
   (assoc-in db [:category :edit] category)))

(re-frame/reg-event-fx
 :get-category-ok
 (fn [{:keys [db]} [_ fx resp]]
   {:db db
    :fx (concat [[:dispatch [:set-category-edit (:data resp)]]] fx)}))

(re-frame/reg-event-fx
 :get-category
 (fn [{:keys [db]} [_ id fx]]
   (f-http/http-get db
                    (f-http/api-uri-admin "categories" id)
                    {}
                    [:get-category-ok fx])))

(re-frame/reg-event-fx
 :update-category-ok
 (fn [{:keys [db]} _]
   {:db db
    :fx [[:dispatch [:push-toast {:content "Category update success"
                                  :type :success}]]
         [:dispatch [:query-categories]]]}))

(re-frame/reg-event-fx
 :update-category
 (fn [{:keys [db]} [_ category]]
   (f-http/http-patch db
                      (f-http/api-uri-admin "categories" (:id category))
                      {:category category}
                      [:update-category-ok])))

(re-frame/reg-event-fx
 :delete-category-ok
 (fn [{:keys [db]} _]
   {:db db
    :fx [[:dispatch [:push-toast {:type :success :content (str "Delete Category success")}]]
         [:dispatch [:set-modal nil]]
         [:dispathc [:set-category-edit nil]]
         [:dispatch [:query-categories]]]}))

(re-frame/reg-event-fx
 :delete-category
 (fn [{:keys [db]} [_ id]]
   (f-http/http-delete db
                       (f-http/api-uri-admin "categories" id)
                       {}
                       [:delete-category-ok])))