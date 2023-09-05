(ns admin.http 
 (:require [ajax.core :as ajax]
           [day8.re-frame.http-fx]
           [admin.util :as util]))

(def ^:private api-base "http://localhost:8080")

(defn api-uri [route & s]
  (apply str api-base route s))

(defn get-headers [db]
  (let [token (get-in db [:login :user :token])
        header (cond-> {:Accept "application/json" :Content-Type "application/json"}
                 token (assoc :authorization (str "Token " token)))
        _ (util/clog "get-headers, header" header)]
    header))

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