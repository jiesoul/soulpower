(ns admin.views.article
    (:require ["moment" :as moment]
              [clojure.string :as str]
              [admin.http :as f-http]
              [admin.shared.buttons :refer [default-button edit-del-modal-btns
                                               edit-button new-button red-button]]
              [admin.shared.form-input :refer [checkbox-input select-input
                                                  text-input
                                                  text-input-backend textarea]]
              [admin.shared.layout :refer [layout-admin layout-home]]
              [admin.shared.modals :as modals]
              [admin.shared.tables :refer [table-admin]]
              [admin.shared.toasts :as toasts]
              [admin.state :as f-state]
              [admin.util :as f-util]
              [re-frame.core :as re-frame]
              [reagent.core :as r]))

(def name-error (r/atom nil))

(re-frame/reg-event-db
 ::get-view-article-ok
 (fn [db [_ resp]]
   (f-util/clog "get view article ok: " resp)
   (assoc-in db [:current-route :result] (:data resp))))

(re-frame/reg-event-fx
 ::get-view-article
 (fn [{:keys [db]} [_ id]]
   (f-util/clog "Get view article: " id)
   (f-http/http-get db
                    (f-http/api-uri "/articles/" id)
                    {}
                    [::get-view-article-ok])))

(re-frame/reg-event-fx
 ::query-articles-ok
 (fn [{:keys [db]} [_ resp]]
   {:db db
    :fx [[:dispatch [::f-state/init-current-route-result (:data resp)]]]}))

(re-frame/reg-event-fx
 ::query-articles
 (fn [{:keys [db]} [_ data]]
   (f-util/clog "query articles: " data)
   (f-http/http-get db
                    (f-http/api-uri "/admin/articles")
                    data
                    [::query-articles-ok])))

(re-frame/reg-event-fx
 ::add-article-ok
 (fn [{:keys [db]} [_ resp]]
   (f-util/clog "add article ok: " resp)
   {:db db 
    :fx [[:dispatch [::toasts/push {:content "保存成功" :type :info}]]]}))

(re-frame/reg-event-fx
 ::add-article
 (fn [{:keys [db]} [_ article]]
   (f-util/clog "add article: " article)
   (f-http/http-post db
                     (f-http/api-uri "/admin/articles")
                     {:article article}
                     [::add-article-ok])))

(re-frame/reg-event-db
 ::get-article-ok
 (fn [db [_ resp]]
   (f-util/clog "get article ok: " resp)
   (assoc-in db [:current-route :edit] (:data resp))))

(re-frame/reg-event-fx
 ::get-article
 (fn [{:keys [db]} [_ id]]
   (f-util/clog "Get a article: " id)
   (f-http/http-get db
                    (f-http/api-uri "/admin/articles/" id)
                    {}
                    [::get-article-ok])))

(re-frame/reg-event-fx
 ::update-article-ok
 (fn [{:keys [db]} [_ resp]]
   (f-util/clog "update article ok: " resp)
   {:db db
    :fx [[:dispatch [::toasts/push {:content "保存成功" :type :success}]]]}))

(re-frame/reg-event-fx
 ::update-article
 (fn [{:keys [db]} [_ article]]
   (f-util/clog "update article: " article)
   (f-http/http-patch db
                      (f-http/api-uri "/admin/articles/" (:id article))
                      {:article article}
                      [::update-article-ok])))


(re-frame/reg-event-fx
 ::push-article-ok
 (fn [{:keys [db]} [_ resp]]
   (f-util/clog "push article ok: " resp)
   {:db db
    :fx [[:dispatch [::toasts/push {:content "Push Success!!!" :type :success}]]]}))

(re-frame/reg-event-fx
 ::push-article
 (fn [{:keys [db]} [_ article]]
   (f-util/clog "push article: " article)
   (f-http/http-patch db
                      (f-http/api-uri "/admin/articles/" (:id article) "/push")
                      {:article article}
                      [::push-article-ok])))

(re-frame/reg-event-fx
 ::delete-article-ok
 (fn [{:keys [db]} [_ resp]]
   (f-util/clog "delete article ok: " resp)
   {:db db
    :fx [[:dispatch [::toasts/push {:type :success
                                    :content "Delete success"}]]
         [:dispatch [::f-state/clean-current-route-edit]]
         [:dispatch [::show-delete-modal false]]]}))

(re-frame/reg-event-fx
 ::delete-article
 (fn [{:keys [db]} [_ id]]
   (f-util/clog "Delete article")
   (f-http/http-delete db
                       (f-http/api-uri "/admin/articles/" id)
                       {}
                       [::delete-article-ok])))

(defn check-name [v]
  (f-util/clog "check name")
  (if (or (nil? v) (str/blank? v))
    (reset! name-error "名称不能为空")
    (reset! name-error nil)))

(defn new-form [] 
  (let [login-user @(re-frame/subscribe [::f-state/login-user])
        article (r/atom {:title ""
                         :author (:username login-user) 
                         :summary ""
                         :detail {:content_md ""}})
        title (r/cursor article [:title])
        summary (r/cursor article [:summary])
        content-md (r/cursor article [:detail :content_md])] 
    [:form
     [:div {:class "flex-l flex-col"} 
      [text-input {:class "pt-2"
                   :name "title"
                   :placeholder "Title"
                   :required ""
                   :on-change #(reset! title (f-util/get-value %))}]
      
      [textarea {:class "pt-4"
                 :placeholder "Summary"
                 :name "summary"
                 :required "" 
                 :on-change #(reset! summary (f-util/get-value %))}] 
      
      ;;  [file-input {:class "pt-4"
      ;;               :help "md"}]
      [textarea {:class "pt-4"
                 :placeholder "Content"
                 :rows 8
                 :name "content"
                 :default-value ""
                 :on-change #(reset! content-md (f-util/get-trim-value %))}]
      [:div {:class "flex justify-center items-center space-x-4 mt-4"}
       [new-button {:on-click #(re-frame/dispatch [::add-article @article])}
        "Save"]]]]))

(defn edit-form [] 
  (let [{:keys [id title summary detail]} @(re-frame/subscribe [::f-state/current-route-edit])
        article (r/atom {:id id 
                         :title title 
                         :summary summary
                         :push_flag 0
                         :push_time nil
                         :detail {:article_id id
                                  :content_md (:content-md detail)}})
        title-edit (r/cursor article [:title])
        summary-edit (r/cursor article [:summary])
        content-edit (r/cursor article [:detail :content_md])]
    [:form
     [:div {:class "flex-l flex-col"}
      [text-input {:class "pt-2"
                   :placeholder "Title"
                   :name "title"
                   :required ""
                   :default-value title
                   :on-change #(reset! title-edit (f-util/get-value %))}]

      [textarea {:class "pt-4"
                 :placeholder "Summary"
                 :name "summary"
                 :required ""
                 :default-value summary
                 :on-change #(reset! summary-edit (f-util/get-value %))}]

      [textarea {:class "pt-4"
                 :placeholder "Content"
                 :rows 8
                 :name "content"
                 :default-value (:content-md detail)
                 :on-change #(reset! content-edit (f-util/get-trim-value %))}]
      [:div {:class "flex justify-center items-center space-x-4 mt-4"}
       [new-button {:on-click #(re-frame/dispatch [::update-article @article])}
        "Save"]]]]))

(defn push-form [] 
  (let [categories @(re-frame/subscribe [::f-state/current-route-categories])
        {:keys [id title summary detail tags]} @(re-frame/subscribe [::f-state/current-route-edit]) 
        article (r/atom {:id id
                         :top_flag 0  
                         :category_id 0
                         :tags tags}) 
        tags-edit (r/cursor article [:tags])
        top-flag-edit (r/cursor article [:top_flag])
        category-id-edit (r/cursor article [:category_id])] 
    [:form
     [:div {:class "flex-l flex-col"}
      [:p "Title: " title]
      [:p "Summary: " summary] 
      [checkbox-input {:class "pt-2"
                       :name "top_flag"
                       :label "Top"
                       :on-change #(reset! top-flag-edit (f-util/get-value %))}]

      [text-input {:class "pt-2"
                   :placeholder "Tags"
                   :name "tags"
                   :default-value tags
                   :on-change #(reset! tags-edit (f-util/get-value %))}]

      [select-input {:class "pt-2"
                     :placeholder "Category" 
                     :name "category"
                     :on-change #(reset! category-id-edit (f-util/get-value %))}
       [:option {:value 0
                 :key 0} "select category"]
       (for [c categories]
         [:option {:value (:id c)
                   :key (:id c)} (:name c)])]
      
      [:div {:class "flex justify-center items-center space-x-4 mt-4"}
       [new-button {:on-click #(re-frame/dispatch [::push-article @article])}
        "Psuh"]]]
     [:p "Content: " (:content-md detail)]]))

(defn delete-form []
  (let [current (re-frame/subscribe [::f-state/current-route-edit])
        title (r/cursor current [:title])]
    [:form
     [:div {:class "p-4 mb-4 text-blue-800 border border-red-300 rounded-lg 
                    bg-red-50 dark:bg-gray-800 dark:text-red-400 dark:border-red-800"}
      [:div {:class "flex items-center"}
       (str "You confirm delete the " @title "? ")]]
     [:div {:class "flex justify-center items-center space-x-4"}
      [red-button {:on-click #(do
                                (re-frame/dispatch [::delete-article (:id @current)]))}
       "Delete"]]]))

(defn query-form []
  (let [q-data (r/atom {:page-size 10 :page 1 :filter "" :sort "create_time DESC"})
        filter (r/cursor q-data [:filter])
        _ (f-util/clog "moment: " (moment "2023-05-08T06:22:51.792024300Z"))]
    [:form
     [:div {:class "flex-1 flex-col my-2 py-2 overflow-x-auto sm:-mx-6 sm:px-6 lg:-mx-8 lg:px-8"}
      [:div {:class "grid grid-cols-4 gap-3"}
       [:div {:class "max-w-10 flex"}
        (text-input-backend {:label "Title"
                             :type "text"
                             :id "title"
                             :on-blur #(when-let [v (f-util/get-trim-value %)]
                                         (swap! filter str " name lk " v))})]]
      [:div {:class "flex inline-flex justify-center items-center w-full"}
       [default-button {:on-click #(re-frame/dispatch [::query-articles @q-data])}
        "Query"]
       [new-button {:on-click #(re-frame/dispatch [::modals/show-modal :new-modal?])}
        "New"]]]]))

(defn actions [d] 
  (let [push-flag (:push-flag d)]
    [:div 
     [edit-del-modal-btns [::get-article (:id d)]]
     (when (zero? push-flag)
       [:<> 
        [:span " | "]
        [edit-button {:on-click #(do
                                   (re-frame/dispatch [::get-article (:id d)])
                                   (re-frame/dispatch [::modals/show-modal :push-modal?]))}
         "Push"] 
        ])]))

(def columns [{:key :id :title "ID"}
              {:key :title :title "Title"}
              {:key :author :title "Author"}
              {:key :create-time :title "Create Time" :format f-util/format-time}
              {:key :like-count :title "Likes"}
              {:key :comment-count :title "Comments"}
              {:key :read-count :title "Reads"}
              {:key :top-flag :title "Top"}
              {:key :push-flag :title "Push"}
              {:key :push-time :title "Push Time" :format f-util/format-time}
              {:key :operation :title "Actioin" :render actions}
              {:key :tags :title "Tags"}
              {:key :summary :title "Summary"}])

(defn index []
  (let [{:keys [list total opts]} @(re-frame/subscribe [::f-state/current-route-result])
        pagination (assoc opts :total total :query-params opts :url ::query-categories)
        data-sources list
        push-modal-show? @(re-frame/subscribe [::f-state/current-push-modal?])]
    [layout-admin
     [:<>
      [modals/modals-crud "Article" new-form edit-form delete-form]
      [modals/modal  {:id "Delete-article"
                      :title "Delete article"
                      :show? push-modal-show?
                      :on-close #(re-frame/dispatch [::modals/close-modal :delete-modal?])}
       [push-form]]]
     [query-form]
     [table-admin {:columns columns
                   :datasources data-sources
                   :pagination pagination}]]))

(defn view []
  (let [{:keys [title create-time tags detail]} @(re-frame/subscribe [::f-state/current-route-result])]
    [layout-home
     [:div {:class "mt-16 mb-6 p-4 max-w-5xl w-full"}
      [:h1 {:class "mb-2 text-2xl text-center font-bold tracking-tight text-gray-900 dark:text-whit"} title]
      [:p {:class "mb-6 line-clamp-3 text-center"} (f-util/format-time create-time)]
      [:div {:class "mb-6 items-left"} 
       (:content-md detail)]
      [:div {:class "mb-6"} tags]]]))




