(ns admin.article-comment.views
    (:require [clojure.string :as str]
              [admin.http :as f-http]
              [admin.shared.buttons :refer [default-button delete-button
                                               edit-button red-button]]
              [admin.shared.form-input :refer [text-input-backend]]
              [admin.shared.layout :refer [layout-admin]]
              [admin.shared.modals :as modals]
              [admin.shared.tables :refer [table-admin]]
              [admin.shared.toasts :as toasts]
              [admin.subs]
              [admin.util :as f-util]
              [re-frame.core :as re-frame]
              [reagent.core :as r]))

(def name-error (r/atom nil))



(defn check-name [v]
  (f-util/clog "check name")
  (if (or (nil? v) (str/blank? v))
    (reset! name-error "名称不能为空")
    (reset! name-error nil)))

(defn delete-form []
  (let [current (re-frame/subscribe [::article-comments-current])
        name (r/cursor current [:name])]
    [:form
     [:div {:class "p-4 mb-4 text-blue-800 border border-red-300 rounded-lg 
                    bg-red-50 dark:bg-gray-800 dark:text-red-400 dark:border-red-800"}
      [:div {:class "flex items-center"}
       (str "You confirm delete the " @name "? ")]]
     [:div {:class "flex justify-center items-center space-x-4"}
      [red-button {:on-click #(do
                                (re-frame/dispatch [::delete-article-comments (:id @current)]))}
       "Delete"]]]))

(defn actions [d] 
  [:div
   [edit-button {:on-click #(do
                              (re-frame/dispatch [::get-article (:id d)])
                              (re-frame/dispatch [:show-modal :edit-modal?]))}
    "Edit"] 
   [:span " | "]
   [delete-button {:on-click #(do
                                (re-frame/dispatch [::get-article (:id d)])
                                (re-frame/dispatch [:show-modal :delete-modal?]))}
    "Del"]])

(def columns [{:key :id :title "ID"}
              {:key :title :title "Article Title"} 
              {:key :create-time :title "Create Time" :format f-util/format-time} 
              {:key :username :title "username"}
              {:key :user-email :title "Email"}
              {:key :operation :title "Actioin" :render actions}
              {:key :content :title "content"}])

(defn index []
  (let [{:keys [list total opts]} @(re-frame/subscribe [:current-route-result])
        delete-modal-show? @(re-frame/subscribe [:current-delete-modal?])
        pagination (assoc opts :total total :query-params opts :url ::query-articles-comments)
        data-sources list
        q-data (r/atom {:page-size 10 :page 1 :filter "" :sort ""})
        filter (r/cursor q-data [:filter])]
    
    [layout-admin 
      [:div 
       [modals/modal delete-modal-show? {:id "Delete-article-comments"
                                         :title "Delete article-comments"
                                         :on-close #(re-frame/dispatch [:close-modal :delete-modal?])}
        [delete-form]]]
     
      ;; page query form
     [:form
      [:div {:class "flex-1 flex-col my-2 py-2 overflow-x-auto sm:-mx-6 sm:px-6 lg:-mx-8 lg:px-8"}
       [:div {:class "grid grid-cols-4 gap-3"}
        [:div {:class "max-w-10 flex"}
         (text-input-backend {:label "name"
                              :type "text"
                              :id "name"
                              :on-blur #(when-let [v (f-util/get-trim-value %)]
                                          (swap! filter str " name lk " v))})]]
       [:div {:class "felx inline-flex justify-center items-center w-full"}
        [default-button {:on-click #(re-frame/dispatch [::query-articles-comments @q-data])}
         "Query"]]]]

      ;; data table
     [table-admin {:columns columns
                   :datasources data-sources
                   :pagination pagination}]]))
