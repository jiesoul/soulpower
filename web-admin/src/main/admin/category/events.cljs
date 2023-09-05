(ns admin.category.events
  (:require [re-frame.core :as re-frame]
            [admin.util :as f-util]
            [admin.http :as f-http]))

(re-frame/reg-event-db
 :query-categories-ok
 (fn [db [_ resp]]
   (assoc-in db [:current-route :result] (:data resp))))

(re-frame/reg-event-fx
 :query-categories
 (fn [{:keys [db]} [_ data]]
   (f-util/clog "query categories: " data)
   (f-http/http-get db
                    (f-http/api-uri "/admin/categories")
                    data
                    [:query-categories-ok])))

(re-frame/reg-event-fx
 ::add-category-ok
 (fn [{:keys [db]} [_ resp]]
   (let [_ (f-util/clog "add category ok: " resp)]
     {:db db
      :fx [[:dispatch [:push-toast {:content "添加成功" :type :info}]]]})))

(re-frame/reg-event-fx
 ::add-category
 (fn [{:keys [db]} [_ category]]
   (f-util/clog "add category: " category)
   (f-http/http-post db
                     (f-http/api-uri "/admin/categories")
                     {:category category}
                     [::add-category-ok])))

(re-frame/reg-event-fx
 ::get-category-ok
 (fn [{:keys [db]} [_ resp]]
   {:db db
    :fx [[:dispatch [:init-current-route-edit (:data resp)]]]}))

(re-frame/reg-event-fx
 ::get-category
 (fn [{:keys [db]} [_ id]]
   (f-util/clog "Get a Category: " id)
   (f-http/http-get db
                    (f-http/api-uri "/admin/categories/" id)
                    {}
                    [::get-category-ok])))

(re-frame/reg-event-fx
 ::update-category-ok
 (fn [{:keys [db]} [_ category]]
   (let [_ (f-util/clog "update category: " category)]
     {:db db
      :fx [[::dispatch [:clean-current-route-edit]]
           [:dispatch [:push-toast {:content (str "Category " (:name category) " 更新成功")
                                      :type :success}]]]})))

(re-frame/reg-event-fx
 ::update-category
 (fn [{:keys [db]} [_ category]]
   (f-http/http-patch db
                      (f-http/api-uri "/admin/categories/" (:id category))
                      {:category category}
                      [::update-category-ok category])))

(re-frame/reg-event-fx
 ::delete-category-ok
 (fn [{:keys [db]} _]
   (let [{:keys [id name]} (-> db :current-route :edit)
         categories (remove #(= id (:id %)) (-> db :current-route :result :list))
         _ (f-util/clog "delete category " name)]
     {:db (assoc-in db [:current-route :result :list] categories)
      :fx [[:dispatch [:push-toast {:type :success :content (str "Delete Category " name)}]]
           [:dispatch [:clean-current-route-edit]]
           [:dispatch [:close-modal :delete-modal?]]]})))

(re-frame/reg-event-fx
 ::delete-category
 (fn [{:keys [db]} [_ id]]
   (f-util/clog "Delete Category")
   (f-http/http-delete db
                       (f-http/api-uri "/admin/categories/" id)
                       {}
                       [::delete-category-ok])))