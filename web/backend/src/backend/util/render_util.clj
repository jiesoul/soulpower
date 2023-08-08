(ns backend.util.render-util
  (:require [ring.util.response :as resp]
            [selmer.parser :as tmpl]))

(def ^:private changes
  "Count the number of changes (since the last reload)."
  (atom 0))

(defn render-page
  [req]
  (let [data (assoc (:params req) :changes @changes)
        view (:application/view req "default")
        html (tmpl/render-file (str "views/user/" view ".html") data)]
    (-> (resp/response (tmpl/render-file "layouts/default.html"
                                         (assoc data :body [:safe html])))
        (resp/content-type "text/html"))))