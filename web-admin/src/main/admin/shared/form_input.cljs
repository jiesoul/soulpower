(ns admin.shared.form-input)

(def css-form-label-backend "block mb-2 text-base font-medium font-bold text-gray-900 dark:text-white")
(def css-form-input-backend "block w-full mb-1 p-1 text-gray-900 border border-gray-300 rounded-lg 
                             bg-gray-50 text-base font-medium focus:ring-blue-500 focus:border-blue-500 
                             dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 
                             dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500")

(def css-form-label "block mb-2 text-base font-medium font-bold text-gray-900 dark:text-white")
(def css-form-input "bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-primary-600 
                     focus:border-primary-600 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 
                     dark:placeholder-gray-400 dark:text-white dark:focus:ring-primary-500 dark:focus:border-primary-500")
(def css-form-select "bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-primary-500 
                      focus:border-primary-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 
                      dark:placeholder-gray-400 dark:text-white dark:focus:ring-primary-500 dark:focus:border-primary-500")
(def css-dropzone-label "flex flex-col items-center justify-center w-full h-64 border-2 border-gray-300 border-dashed 
                         rounded-lg cursor-pointer bg-gray-50 dark:hover:bg-bray-800 dark:bg-gray-700 hover:bg-gray-100 
                         dark:border-gray-600 dark:hover:border-gray-500 dark:hover:bg-gray-600")

(def css-form-textarea "block p-2.5 w-full text-sm text-gray-900 bg-gray-50 rounded-lg border border-gray-300 
                        focus:ring-primary-500 focus:border-primary-500 dark:bg-gray-700 dark:border-gray-600 
                        dark:placeholder-gray-400 dark:text-white dark:focus:ring-primary-500 dark:focus:border-primary-500")

(def css-form-errors "mt-2 text-sm text-red-600 dark:text-red-500")

(def css-checkbox "w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 
                   dark:peer-focus:ring-blue-800 rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full 
                   peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] 
                   after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all 
                   dark:border-gray-600 peer-checked:bg-blue-600")

(def css-input "fblock w-full rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 
                placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6")

(defn form-input [{:keys [label name type on-change]}]
  (fn []
    [:div
     [:label {:class "block text-sm font-medium leading-6 text-gray-900"} label]
     [:div {:class "mt-2"}
      [:input {:class css-input
               :type type
               :name name
               :on-change on-change}]]]))

(defn checkbox-input [{:keys [label name class] :as props}] 
  [:div {:class class}
   [:label {:class "relative inline-flex items-center cursor-pointer"}
    [:input (merge props {:id name
                          :type "checkbox"
                          :name name
                          :class "sr-only peer"})]
    [:div {:class css-checkbox}]
    [:span {:class "ml-3 text-sm font-medium text-gray-900 dark:text-gray-300"}
     label]]])

(defn text-input [{:keys [label name class errors] :as props}]
  [:div {:class class}
   (when label
     [:label {:for name
              :class css-form-label} label])
   [:input (merge props {:id name
                         :type "text"
                         :name name
                         :class css-form-input})]
   (when errors
     [:p {:class css-form-errors} errors])])

(defn select-input [{:keys [label name class errors] :as props} & children]
  [:div {:class class}
   (when label
     [:label {:class css-form-label
              :for name} label])
   (into [:select (merge props {:id name
                                :name name
                                :class css-form-select})]
         children)
   (when errors
     [:p {:class css-form-errors} errors])])

(defn file-input [{:keys [label name class help errors] :as props}]
  [:div {:class class}
   (when label
     [:label {:for name 
              :class css-form-label} label])
   [:input (merge props {:id name 
                         :name name 
                         :type "file"
                         :class css-form-input})]
   [:p {:class "mt-1 text-sm text-gray-500 dark:text-gray-300"} help]
   (when errors
     [:p {:class css-form-errors} errors])])

(defn dropzone [{:keys [label name class help ]} & children]
  [:div {:class class}
   [:label {:for name 
            :class css-dropzone-label}
    [:div {:class "flex flex-col items-center justify-center pt-5 pb-6"}
     [:p {:class "mb-2 text-sm text-gray-500 dark:text-gray-400"}
      label]
     [:p {:class "text-xs text-gray-500 dark:text-gray-400"} help]]
    [:input {:id name
             :type "file"
             :class "hidden"}]]])

(defn textarea [{:keys [label name class errors] :as props}]
  [:div {:class class}
   (when label
     [:label {:for name
              :class css-form-label} label])
   [:textarea (merge props {:id name
                            :name name
                            :class css-form-textarea})]
   (when errors
     [:p {:class css-form-errors} errors])])

(defn text-input-backend [{:keys [label name class errors] :as props}]
  [:div {:class (if class class "flex items-center")}
   (when label 
     [:label {:class css-form-label-backend
              :for "name"} (str label "：")])
   [:input (merge {:id name
                   :name name
                   :default-value ""
                   :class css-form-input-backend
                   :type "text"}
                  props)]
   (when errors [:div {:class css-form-errors}])])

(defn query-input-text [{:keys [name label] :as props}]
  [:div {:class "flex items-center"}
   (when label
     [:label {:class css-form-label-backend
              :for "name"} (str label "：")])
   [:input (merge {:id name
                   :name name
                   :class css-form-input-backend
                   :type "text"}
                  props)]])