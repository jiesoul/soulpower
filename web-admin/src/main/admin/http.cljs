(ns admin.http 
 (:require [ajax.core :as ajax]
           [day8.re-frame.http-fx]
           [admin.util :as util]
           [clojure.string :as str]
           [cljs.reader :as rdr]))

(def ^:private api-base "http://localhost:8080")

(defn api-uri [& params]
  (str/join "/" (cons api-base params)))

(defn api-uri-admin [& params]
  (str/join "/" (cons (str api-base "/admin") params)))

(defn get-headers [db]
  (let [token (get-in db [:login-user :token])
        header (cond-> {:Accept "application/json" :Content-Type "application/json"}
                 token (assoc :authorization (str "Token " token)))
        _ (util/clog "get-headers, header" header)]
    header))

(defn add-epoch
  "Add :epoch timestamp based on :createdAt field."
  [item]
  (assoc item :epoch (-> item :createdAt rdr/parse-timestamp .getTime)))

(defn index-by
  "Index collection by function f (usually a keyword) as a map"
  [f coll]
  (into {}
        (map (fn [item]
               (let [item (add-epoch item)]
                 [(f item) item])))
        coll))

(defn http [method db uri data on-success & on-failure]
  (let [xhrio (cond-> {:debug true
                       :method method
                       :uri uri
                       :headers (get-headers db)
                       :format (ajax/json-request-format)
                       :response-format (ajax/json-response-format {:keywords? true})
                       :on-success on-success
                       :on-failure (if on-failure on-failure [:req-failed-message])}
                      data (assoc :params data))]
    {:http-xhrio xhrio
     :db (assoc db :loading true)}))

(def http-post (partial http :post))
(def http-get (partial http :get))
(def http-delete (partial http :delete))
(def http-put (partial http :put))
(def http-patch (partial http :patch))