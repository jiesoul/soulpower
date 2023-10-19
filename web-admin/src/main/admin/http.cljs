(ns admin.http
  (:require [ajax.core :as ajax]
            [admin.util :as util]
            [clojure.string :as str]
            [cljs.reader :as rdr]
            [re-frame.core :as re-frame]))

(def ^:private api-base "http://localhost:8088")

(defn api-uri [& params]
  (str/join "/" (cons api-base params)))

(defn api-uri-admin [& params]
  (str/join "/" (cons (str api-base "/admin") params)))

(defn gen-headers [db]
  (let [token (get-in db [:login-user :token])
        header (cond-> {:Accept "application/json" :Content-Type "application/json"}
                 token (assoc :authorization (str "Token " token)))]
    header))

(defn add-epoch
  "Add :epoch timestamp based on :createdAt field."
  [item]
  (assoc item :epoch (-> item :createdAt rdr/parse-timestamp .getTime)))

;; (defn gen-get-uri [data]
;;   (let [{:keys [page page-size filter sort]}]
;;     ))

(defn gen-fitler-sort [{:keys [filter sort] :as data}]
  (let [data (if filter (assoc data :filter (str/join " and " (vals filter))) data)
        data (if sort (assoc data :sort (str/join "," (vals sort))) data)]
    data))

(defn http [method db uri data on-success & on-failure]
  (let [xhrio (cond-> {:debug true
                       :method method
                       :uri uri
                       :headers (gen-headers db)
                       :format (ajax/json-request-format)
                       :response-format (ajax/json-response-format {:keywords? true})
                       :on-success on-success
                       :on-failure (if on-failure
                                     on-failure
                                     [:req-failed-message])}
                data (assoc :params (if (= method :get) (gen-fitler-sort data) data)))
        _ (util/clog "http" uri)
        _ (util/clog "data" data)]
    {:http-xhrio xhrio
     :db db}))

(def http-post (partial http :post))
(def http-get (partial http :get))
(def http-delete (partial http :delete))
(def http-put (partial http :put))
(def http-patch (partial http :patch))
