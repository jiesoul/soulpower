(ns admin.user.views
  (:require [admin.events]
            [admin.shared.buttons :refer [btn-edit btn-query]]
            [admin.shared.css :as css]
            [admin.shared.form-input :refer [form-input query-input-text
                                             text-input-backend]]
            [admin.shared.layout :refer [layout]]
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

(defn edit-form []
  (let [user (re-frame/subscribe [:user/edit])]
    (fn []
      (when-let [edit (r/atom @user)]
        [:form
         [:div {:class "grid gap-4 mb-4 sm:grid-cols-2"}
          (text-input-backend {:label "Name"
                               :name "name"
                               :default-value (:name @user)
                               :on-change #(swap! edit assoc-in [:name] (f-util/get-value %))})
          (text-input-backend {:label "Description"
                               :name "descrtiption"
                               :default-value (:description @user)
                               :on-change #(swap! edit assoc-in [:description] (f-util/get-value %))})]
         [:div {:class "flex justify-center items-center space-x-4"}
          [btn-edit {:on-click #(re-frame/dispatch [:update-user @edit])}
           "Save"]]]))))

(defn action-fn [e]
  [:div
   [btn-edit {:on-click #(re-frame/dispatch [:get-user
                                             (:id e)
                                             [[:dispatch [:set-modal {:show? true
                                                                      :title "user Edit"
                                                                      :child edit-form}]]]])}
    "Edit"]])

(def columns [{:key :email :title "Email"}
              {:key :username :title "Name"}
              {:key :operation :title "Actions" :render action-fn}])

(defn query-form []
  ;; page query form
  (let [default-pagination (re-frame/subscribe [:default-pagination])]
    (fn []
      (let [q-data (r/atom (assoc @default-pagination
                                  :event :query-users))]
        [:form
         [:div {:class "grid grid-cols-4 gap-3"}
          [:div {:class "max-w-10 flex"}
           [query-input-text {:label "name"
                              :name "name"
                              :on-change #(swap! q-data assoc-in [:filter :name] (f-util/get-filter-like "name" %))}]]]
         [:div {:class "felx inline-flex justify-center items-center w-full"}
          [btn-query {:on-click #(re-frame/dispatch [:query-users @q-data])} "Query"]]]))))

(defn data-table []
  (let [datasources (re-frame/subscribe [:user/datasources])]
    (fn []
      [table-admin (assoc @datasources :columns columns)])))

(defn index []
  [layout
   [:<>
    [query-form]
    [data-table]]])

(defn profile []
  (let [login-user (re-frame/subscribe [:login-user])]
    (fn []
      (let [user-data (r/atom @login-user)]
        [:div {:class css/form-b-c}
          [:div {:class css/form-b-m}
           [:div {:class "space-y-4"}
            [form-input {:label "Username"
                         :type "text"
                         :name "username"
                         :value (:username @login-user)
                         :readonly true}]
            [form-input {:label "Password"
                         :type "password"
                         :name "password"
                         :on-change #(swap! user-data assoc :old-pass (.. % -target -value))}]
            [:div
             [:button {:class css/btn-b-c
                       :on-click #(re-frame/dispatch [:update-user! @user-data])}
              "Save"]]]]]))))

(defn profile-page []
  [layout 
   [:<> 
    [profile]]])

(defn change-password []
  (let [login-user (re-frame/subscribe [:login-user])]
    (fn []
      (let [password-data (r/atom {:id (:id @login-user)
                                   :old-pass ""
                                   :new-pass ""
                                   :confirm-pass ""})]
        [:div {:class css/form-b-c}
         [:div {:class css/form-b-t} 
          [:h2 {:class css/form-b-t-h-2} (:username @login-user)]]
         [:div {:class css/form-b-m}
          [:div {:class "space-y-4"}
           [form-input {:label "Old Password"
                        :type "password"
                        :name "old-pass"
                        :required true
                        :on-change #(swap! password-data assoc :old-pass (.. % -target -value))}]
           [form-input {:label "New Password"
                        :type "password"
                        :name "new-pass"
                        :required true
                        :on-change #(swap! password-data assoc :new-pass (.. % -target -value))}]
           [form-input {:label "Confirm New Password"
                        :type "password"
                        :name "confirm-pass"
                        :required true
                        :on-change #(swap! password-data assoc :confirm-pass (.. % -target -value))}]
           [:div
            [:button {:class css/btn-b-c
                      :on-click #(re-frame/dispatch [:update-user-password! @password-data])}
             "Save"]]]]]))))

(defn password-page []
  [layout 
   [:<>
    [change-password]]])
