(ns admin.article.views
  (:require [admin.events]
            [admin.shared.buttons :refer [btn-del btn-edit btn-new btn-query
                                          red-button]]
            [admin.shared.css :as css]
            [admin.shared.form-input :refer [checkbox-input query-input-text
                                             select-input text-input
                                             textarea]]
            [admin.shared.layout :as layout]
            [admin.shared.tables :refer [table-admin]]
            [admin.subs]
            [admin.util :as f-util]
            [clojure.string :as str]
            [re-frame.core :as re-frame]
            [reagent.core :as r]))

(def name-error (r/atom nil))

(defn check-name [v]
  (f-util/clog "check name")
  (if (or (nil? v) (str/blank? v))
    (reset! name-error "名称不能为空")
    (reset! name-error nil)))

(defn new-form []
  (let [login-user @(re-frame/subscribe [:login-user])
        article (r/atom {:title ""
                         :author (:username login-user)
                         :summary ""
                         :detail {:content-md ""}})
        title (r/cursor article [:title])
        summary (r/cursor article [:summary])
        content-md (r/cursor article [:detail :content-md])]
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
       [btn-new {:on-click #(re-frame/dispatch [:add-article @article])}
        "Save"]]]]))

(defn edit-form []
  (let [{:keys [id title summary detail]} @(re-frame/subscribe [:article/edit])
        article (r/atom {:id id
                         :title title
                         :summary summary
                         :detail {:content-md (:content-md detail)}})
        title-edit (r/cursor article [:title])
        summary-edit (r/cursor article [:summary])
        content-edit (r/cursor article [:detail :content-md])]
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
       [btn-new {:on-click #(re-frame/dispatch [:update-article @article])}
        "Save"]]]]))

(defn push-form []
  (let [categories @(re-frame/subscribe [:article/categories])
        {:keys [id title summary detail tags]} @(re-frame/subscribe [:article/edit])
        article (r/atom {:id id
                         :top-flag 0
                         :category-id 0
                         :tags tags})
        tags-edit (r/cursor article [:tags])
        top-flag-edit (r/cursor article [:top-flag])
        category-id-edit (r/cursor article [:category-id])]
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
                     :placeholder "Article"
                     :name "category"
                     :on-change #(reset! category-id-edit (f-util/get-value %))}
       [:option {:value 0
                 :key 0} "select category"]
       (for [c categories]
         [:option {:value (:id c)
                   :key (:id c)} (:name c)])]

      [:div {:class "flex justify-center items-center space-x-4 mt-4"}
       [btn-new {:on-click #(re-frame/dispatch [::push-article @article])}
        "Psuh"]]]
     [:p "Content: " (:content-md detail)]]))

(defn delete-form []
  (let [current (re-frame/subscribe [:article/edit])
        title (r/cursor current [:title])]
    [:form
     [:div {:class "p-4 mb-4 text-blue-800 border border-red-300 rounded-lg 
                    bg-red-50 dark:bg-gray-800 dark:text-red-400 dark:border-red-800"}
      [:div {:class "flex items-center"}
       (str "You confirm delete the " @title "? ")]]
     [:div {:class "flex justify-center items-center space-x-4"}
      [red-button {:on-click #(do
                                (re-frame/dispatch [:delete-article (:id @current)]))}
       "Delete"]]]))

(defn action-fn [e]
  [:div
   [btn-edit {:on-click #(re-frame/dispatch [:get-article
                                             (:id e)
                                             [[:dispatch [:set-modal {:show? true
                                                                      :title "Article Edit"
                                                                      :child edit-form}]]]])}
    "Edit"]
   (when (zero? (:push-flag e))
     [:<>
      [:span {:class css/divi} "|"]
      [btn-edit {:on-click #(re-frame/dispatch [:get-article
                                                (:id e)
                                                [[:dispatch [:set-modal {:show? true
                                                                         :title "Article Edit"
                                                                         :child push-form}]]]])}
       "Push"]])
   [:span {:class css/divi} "|"]
   [btn-del {:on-click #(re-frame/dispatch [:get-article
                                            (:id e)
                                            [[:dispatch [:set-modal {:show? true
                                                                     :title "Article Delete"
                                                                     :child delete-form}]]]])}
    "Del"]])

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
              {:key :operation :title "Actioin" :render action-fn}
              {:key :tags :title "Tags"}
              {:key :summary :title "Summary"}])

(defn query-form []
  ;; page query form
  (let [default-pagination (re-frame/subscribe [:default-pagination])
        categories (re-frame/subscribe [:category/list])]
    (fn []
      (let [q-data (r/atom (assoc @default-pagination
                                  :event :query-articles))]
        [:form
         [:div {:class "grid grid-cols-4 gap-3"}
          [:div {:class "max-w-10 flex"}
           [query-input-text {:label "name"
                              :name "name"
                              :on-change #(swap! q-data assoc-in [:filter :name] (f-util/get-filter-like "name" %))}]]

          (when @categories
            [select-input {:class "pt-2"}
             :placeholder "Article Catergory"
             :name "category-id"
             [:option {:value 0 :key 0} "select category"]
             (doall
              (for [{:keys [id name]} @categories]
                [:option {:value id
                          :key (str "ct-" id)} name]))])]

         [:div {:class "felx inline-flex justify-center items-center w-full mt-4"}
          [btn-query {:on-click #(re-frame/dispatch [:query-articles @q-data])} "Query"]
          [btn-new {:on-click #(re-frame/dispatch [:set-modal {:show? true
                                                               :title "Article Add"
                                                               :child new-form}])
                    :class css/button-green} "New"]]]))))

(defn data-table []
  (let [datasources (re-frame/subscribe [:article/datasources])]
    (fn []
      [table-admin (assoc @datasources :columns columns)])))

(defn index []
  [layout/layout
   [:<>
    [query-form]
    [data-table]]])
