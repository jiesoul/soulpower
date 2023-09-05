(ns admin.db
  (:require [cljs.reader]
            [cljs.spec.alpha :as s]
            [admin.util :as util]))

(s/def ::id int?)
(s/def ::title string?)
(s/def ::done boolean?)

(def MAX-TOASTS 5)
(def MAX-TIMEOUT 5000)
(def TIMEOUT 3)

(def default-db {:toasts (vec [])
                 :login nil
                 :current-route nil})

(def login-status-key "login-status")

(defn login->local-store 
  [login-status]
  (.setItem js/localStorage login-status-key (str login-status)))

(defn remove->login-local-store 
  []
  (.removeItme js/localStorage login-status-key))


