(ns admin.category.views 
  (:require [clojure.string :as str]
            [admin.events]
            [admin.subs]
            [admin.shared.buttons :refer [btn edit-del-modal-btns new-button
                                             red-button]]
            [admin.shared.css :as css]
            [admin.shared.form-input :refer [text-input-backend]]
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
        [new-button {:on-click #(re-frame/dispatch [:add-category @edit])}
         "Save"]]])))

(defn edit-form []
  (let [current (re-frame/subscribe [:current-route-edit]) 
        edit (r/atom @current)
        name (r/cursor edit [:name])
        description (r/cursor edit [:description])]
    (when @current
      [:form
       [:div {:class "grid gap-4 mb-4 sm:grid-cols-2"}
        (text-input-backend {:label "Name"
                             :name "name"
                             :default-value (:name @current)
                             :on-change #(reset! name (f-util/get-value %))})
        (text-input-backend {:label "Description"
                             :name "descrtiption"
                             :default-value (:description @current)
                             :on-change #(reset! description (f-util/get-value %))})]
       [:div {:class "flex justify-center items-center space-x-4"}
        [new-button {:on-click #(re-frame/dispatch [:update-category @current])}
         "Save"]]])))

(defn delete-form []
  (let [current (re-frame/subscribe [:current-route-edit]) 
        name (r/cursor current [:name])]
    (when @current
      [:form
       [:div {:class "p-4 mb-4 text-blue-800 border border-red-300 rounded-lg 
                    bg-red-50 dark:bg-gray-800 dark:text-red-400 dark:border-red-800"}
        [:div {:class "flex items-center"}
         (str "You confirm delete the " @name "? ")]]
       [:div {:class "flex justify-center items-center space-x-4"}
        [red-button {:on-click #(re-frame/dispatch [:delete-category (:id @current)])}
         "Delete"]]])))

(defn action-fn [d]
  [edit-del-modal-btns [::get-category (:id d)]])

(def columns [{:key :name :title "Name"}
              {:key :description :title "Description"}
              {:key :operation :title "Actions" :render action-fn}])

(defn query-form []
  ;; page query form
  (let [q-data (r/atom {:page-size 10 :page 1 :filter "" :sort ""})
        filter (r/cursor q-data [:filter])]
    [:<> 
      [:div {:class "flex-1 flex-col my-2 py-2 overflow-x-auto sm:-mx-6 sm:px-6 lg:-mx-8 lg:px-8"}
       [:div {:class "grid grid-cols-4 gap-3"}
        [:div {:class "max-w-10 flex"}
         (text-input-backend {:label "name"
                              :type "text"
                              :id "name"
                              :on-blur #(when-let [v (f-util/get-trim-value %)]
                                          (swap! filter str " name lk " v))})]]
       [:div {:class "felx inline-flex justify-center items-center w-full"}
        [btn {:on-click #(re-frame/dispatch [:query-categories @q-data])
              :class css/buton-purple} "Query"]
        [btn {:on-click #(re-frame/dispatch [:set-current-route-modal {:show? true?
                                                                       :title "Category Add"
                                                                       :child new-form}])
              :class css/button-green} "New"]]]
      
     [:div {:class "h-px my-4 bg-blue-500 border-0 dark:bg-blue-700"}]]))

(defn data-table []
  (let [datasources (re-frame/subscribe [:datasources])]
    (fn []
      [table-admin {:columns columns
                    :datasources (:list @datasources)
                    :pagination (:pagination @datasources)}])))

(defn index [] 
    [layout
     [:<>
      [query-form]
      [data-table]]])