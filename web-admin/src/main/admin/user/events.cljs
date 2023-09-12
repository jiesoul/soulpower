(ns admin.user.events
  (:require [re-frame.core :as re-frame]
            [admin.util :as f-util]
            [admin.http :as f-http]
            [clojure.string :as str]))

(re-frame/reg-event-db
 :init-user
 (fn [db _]
   (dissoc db :user)))

(defn gen-query-str [{:keys [filter page page-size sort]}]
  (let [qs (str "?page=" page "&page-size=" page-size)
        qs (if filter (str "&filter=" (str/join " and " (vals filter))) qs)
        qs (if sort (str "&filter=" (str/join "," (vals sort))) qs)]
    qs))

(re-frame/reg-event-db
 :query-users-ok
 (fn [db [_ resp]]
   (assoc-in db [:user :data] (:data resp))))

(re-frame/reg-event-fx
 :query-users
 (fn [{:keys [db]} [_ data]]
   (let [query-str (gen-query-str data)]
     (f-http/http-get (assoc-in db [:user :query] data)
                      (f-http/api-uri-admin "users")
                      data
                      [:query-users-ok]))))

(re-frame/reg-event-fx
 :add-user-ok
 (fn [{:keys [db]} [_ resp]]
   (let [_ (f-util/clog "add user ok: " resp)]
     {:db db
      :fx [[:dispatch [:push-toast {:content "添加成功" :type :info}]]
           [:dispatch [:set-modal nil]]]})))

(re-frame/reg-event-fx
 :add-user
 (fn [{:keys [db]} [_ user]]
   (f-http/http-post db
                     (f-http/api-uri "admin" "users")
                     {:user user}
                     [:add-user-ok])))

(re-frame/reg-event-db
 :set-user-edit
 (fn [db [_ user]]
   (assoc-in db [:user :edit] user)))

(re-frame/reg-event-fx
 :get-user-ok
 (fn [{:keys [db]} [_ fx resp]]
   {:db db
    :fx (concat [[:dispatch [:set-user-edit (:data resp)]]] fx)}))

(re-frame/reg-event-fx
 :get-user
 (fn [{:keys [db]} [_ id fx]]
   (f-http/http-get db
                    (f-http/api-uri-admin "users" id)
                    {}
                    [:get-user-ok fx])))

(re-frame/reg-event-fx
 :update-user-ok
 (fn [{:keys [db]} _]
   {:db db
    :fx [[:dispatch [:push-toast {:content "user update success"
                                  :type :success}]]
         [:dispatch [:query-users]]]}))

(re-frame/reg-event-fx
 :update-user
 (fn [{:keys [db]} [_ user]]
   (f-http/http-patch db
                      (f-http/api-uri-admin "users" (:id user))
                      {:user user}
                      [:update-user-ok])))

(re-frame/reg-event-fx
 :update-user-password-ok
 (fn [{:keys [db]} _]
   {:db db
    :fx [[:dispatch [:push-toast {:type :success :content (str "Delete user success")}]]
         [:dispatch [:set-modal nil]]
         [:dispathc [:set-user-edit nil]]
         [:dispatch [:query-users]]]}))

(re-frame/reg-event-fx
 :update-user-password!
 (fn [{:keys [db]} [_ data]]
   (f-http/http-patch db
                       (f-http/api-uri-admin "users" (:id data) "password")
                       data
                       [:update-user-password-ok])))