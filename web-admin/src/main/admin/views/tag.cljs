(ns admin.views.tag 
  (:require [clojure.string :as str]
            [admin.http :as f-http]
            [admin.shared.buttons :refer [btn edit-del-modal-btns new-button red-button]]
            [admin.shared.css :as css]
            [admin.shared.form-input :refer [text-input-backend]]
            [admin.shared.layout :refer [layout-admin]]
            [admin.shared.modals :as modals]
            [admin.shared.tables :refer [table-admin]]
            [admin.shared.toasts :as toasts]
            [admin.state :as f-state]
            [admin.util :as f-util]
            [re-frame.core :as re-frame]
            [reagent.core :as r]))

(def name-error (r/atom nil))

(re-frame/reg-event-fx
 ::query-tags-ok
 (fn [{:keys [db]} [_ resp]]
   {:db db
    :fx [[:dispatch [::f-state/init-current-route-result (:data resp)]]]}))

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
    :fx [[:dispatch [::toasts/push {:content (str  "Tag " (:name tag) " 添加成功") :type :info}]]]}))

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
    :fx [[:dispatch [::f-state/init-current-route-edit (:data resp)]]]}))

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
    :fx [[:dispatch [::toasts/push {:content (str "Tag:" (:name tag)  " 更新成功") 
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
      :fx [[:dispatch [::toasts/push {:type :success :content (str "Tag: " name " Delete success")}]]
           [:dispatch [::f-state/clean-current-route-edit]]
           [:dispatch [::modals/close-modal :delete-modal?]]]})))

(re-frame/reg-event-fx
 ::delete-tag
 (fn [{:keys [db]} [_ {:keys [id] :as t}]]
   (f-http/http-delete db
                       (f-http/api-uri "/admin/tags/" id)
                       {}
                       [::delete-tag-ok t])))

(defn check-name [v]
  (f-util/clog "check name")
  (if (or (nil? v) (str/blank? v))
    (reset! name-error "名称不能为空")
    (reset! name-error nil)))

(defn new-form []
  (let [edit (r/atom {:name ""
                      :description ""})
        name (r/cursor edit [:name])
        description (r/cursor edit [:description])]
    [:form
     [:div {:class "grid gap-4 mb-6 sm:grid-cols-2"}

      [:div
       (text-input-backend {:label "Name"
                            :name "name"
                            :required true
                            :default-value ""
                            :on-blur #(check-name (f-util/get-value %))
                            :on-change #(reset! name (f-util/get-value %))})
       (when @name-error
         [:p {:class "mt-2 text-sm text-red-600 dark:text-red-500"}
          [:span {:class "font-medium"}]
          @name-error])]
      [:div
       (text-input-backend {:label "Description"
                            :name "descrtiption"
                            :default-value ""
                            :on-change #(reset! description (f-util/get-value %))})]]
     [:div {:class "flex justify-center items-center space-x-4 mt-4"}
      [new-button {:on-click #(re-frame/dispatch [::new-tag @edit])}
       "Add"]]]))

(defn edit-form []
  (let [current @(re-frame/subscribe [::f-state/current-route-edit])
        edit (r/atom current)
        name (r/cursor edit [:name])
        description (r/cursor edit [:description])]
    [:form
     [:div {:class "grid gap-4 mb-4 sm:grid-cols-2"}
      (text-input-backend {:label "Name"
                           :name "name"
                           :default-value (:name current)
                           :on-change #(reset! name (f-util/get-value %))})
      (text-input-backend {:label "Description"
                           :name "descrtiption"
                           :default-value (:description current)
                           :on-change #(reset! description (f-util/get-value %))})]
     [:div {:class "flex justify-center items-center space-x-4"}
      [new-button {:on-click #(re-frame/dispatch [::update-tag @edit])}
       "Update"]]]))

(defn delete-form []
  (let [current (re-frame/subscribe [::f-state/current-route-edit])
        name (r/cursor current [:name])]
    [:form
     [:div {:class "p-4 mb-4 text-blue-800 border border-red-300 rounded-lg 
                    bg-red-50 dark:bg-gray-800 dark:text-red-400 dark:border-red-800"}
      [:div {:class "flex items-center"}
       (str "You confirm delete the " @name "? ")]]
     [:div {:class "flex justify-center items-center space-x-4"}
      [red-button {:on-click #(do
                                (re-frame/dispatch [::delete-tag @current]))}
       "Delete"]]]))

(defn action-fn [d]
  [edit-del-modal-btns [::get-tag (:id d)]])

(def columns [{:key :name :title "Name"}
              {:key :description :title "Description"}
              {:key :operation :title "Actions" :render action-fn}])

(defn index []
  (let [{:keys [list total opts]} @(re-frame/subscribe [::f-state/current-route-result])
        pagination (assoc opts :total total :query-params opts :url ::query-tags)
        data-sources list
        q-data (r/atom {:page-size 10 :page 1 :filter "" :sort ""})
        filter (r/cursor q-data [:filter])]
    [layout-admin 
     [modals/modals-crud "Tag" new-form edit-form delete-form]
      ;; page query form
     [:form
      [:div {:class "flex-1 flex-col my-2 py-2 overflow-x-auto sm:-mx-6 sm:px-6 lg:-mx-8 lg:px-8"}
       [:div {:class "grid grid-cols-4 gap-3"}
        [:div {:class "max-w-10 flex"}
         (text-input-backend {:label "Name："
                              :type "text"
                              :id "name"
                              :on-blur #(when-let [v (f-util/get-trim-value %)]
                                          (swap! filter str " name lk " v))})]]
       [:div {:class "felx inline-flex justify-center items-center w-full"}
        (btn {:on-click #(re-frame/dispatch [::query-tags @q-data])
              :class css/buton-purple} "Query")
        (btn {:on-click #(re-frame/dispatch [::modals/show-modal :new-modal?])
              :class css/button-green} "New")]]]
      ;; data table 
     [table-admin {:columns columns
                   :datasources data-sources
                   :pagination pagination}]]))