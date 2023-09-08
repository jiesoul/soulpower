(ns admin.category.views 
  (:require [clojure.string :as str]
            [admin.events]
            [admin.subs]
            [admin.shared.buttons :refer [btn-new btn-query btn-edit btn-del
                                             red-button]]
            [admin.shared.css :as css]
            [admin.shared.form-input :refer [text-input-backend query-input-text]]
            [admin.shared.layout :refer [layout]]
            [admin.shared.tables :refer [table-admin]]
            [admin.util :as f-util]
            [re-frame.core :as re-frame]
            [reagent.core :as r]))

(def name-error (r/atom nil))

(defn check-name [v]
  (f-util/clog "check name")
  (if (or (nil? v) (str/blank? v)) 
    (reset! name-error "名称不能为空")
    (reset! name-error nil)))

(defn new-form []
  (let [edit (r/atom {:name ""
                      :description ""})]
    (fn []
      [:form
       [:div {:class "grid gap-4 mb-6 sm:grid-cols-2"}
        [:div
         (text-input-backend {:label "Name："
                              :name "name"
                              :required true
                              :on-blur #(check-name (f-util/get-value %))
                              :on-change #(swap! edit assoc :name (f-util/get-value %))})]
        [:div
         (text-input-backend {:label "Description"
                              :name "descrtiption"
                              :on-change #(swap! edit assoc :description (f-util/get-value %))})]]
       [:div {:class "flex justify-center items-center space-x-4 mt-4"}
        [btn-new {:on-click #(re-frame/dispatch [:add-category @edit])}
         "Save"]]])))

(defn edit-form []
  (let [category (re-frame/subscribe [:category/edit])]
    (fn []
      (when-let [edit (r/atom @category)]
        [:form
         [:div {:class "grid gap-4 mb-4 sm:grid-cols-2"}
          (text-input-backend {:label "Name"
                               :name "name"
                               :default-value (:name @category)
                               :on-change #(swap! edit assoc-in [:name] (f-util/get-value %))})
          (text-input-backend {:label "Description"
                               :name "descrtiption"
                               :default-value (:description @category)
                               :on-change #(swap! edit assoc-in [:description] (f-util/get-value %))})]
         [:div {:class "flex justify-center items-center space-x-4"}
          [btn-new {:on-click #(re-frame/dispatch [:update-category @edit])}
           "Save"]]]))))

(defn delete-form []
  (let [current (re-frame/subscribe [:category/edit])]
    (fn []
      (when @current
        [:form
         [:div {:class "p-4 mb-4 text-blue-800 border border-red-300 rounded-lg 
                    bg-red-50 dark:bg-gray-800 dark:text-red-400 dark:border-red-800"}
          [:div {:class "flex items-center"}
           (str "You confirm delete the " (:name @current) "? ")]]
         [:div {:class "flex justify-center items-center space-x-4"}
          [red-button {:on-click #(re-frame/dispatch [:delete-category (:id @current)])}
           "Delete"]]]))))

(defn action-fn [e]
  [:div
   [btn-edit {:on-click #(re-frame/dispatch [:get-category
                                             (:id e)
                                             [[:dispatch [:set-modal {:show? true
                                                                      :title "Category Edit"
                                                                      :child edit-form}]]]])} 
    "Edit"]
   [:span {:class css/divi} "|"]
   [btn-del {:on-click #(re-frame/dispatch [:get-category
                                            (:id e)
                                            [[:dispatch [:set-modal {:show? true
                                                                    :title "Category Delete"
                                                                    :child delete-form}]]]])} 
    "Del"]])

(def columns [{:key :name :title "Name"}
              {:key :description :title "Description"}
              {:key :operation :title "Actions" :render action-fn}])

(defn query-form []
  ;; page query form
  (let [q-data (r/atom {})]
    (fn []
      [:form
       [:div {:class "grid grid-cols-4 gap-3"}
        [:div {:class "max-w-10 flex"}
         [query-input-text {:label "name"
                            :name "name"
                            :on-change #(swap! q-data [:filter] assoc :name (f-util/get-trim-value %))}]]]
       [:div {:class "felx inline-flex justify-center items-center w-full"}
        [btn-query {:on-click #(re-frame/dispatch [:query-categories @q-data])} "Query"]
        [btn-new {:on-click #(re-frame/dispatch [:set-modal {:show? true
                                                         :title "Category Add"
                                                         :child new-form}])
              :class css/button-green} "New"]]])))

(defn data-table []
  (let [datasources (re-frame/subscribe [:category/list])
        pagination (re-frame/subscribe [:category/query])]
    (fn []
      [table-admin {:columns columns
                    :datasources @datasources
                    :pagination  @pagination}])))

(defn index [] 
    [layout
     [:<>
      [query-form]
      [data-table]]])