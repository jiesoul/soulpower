(ns admin.db
  (:require [cljs.reader]
            [cljs.spec.alpha :as s]
            [re-frame.core :refer [reg-cofx]]))

(s/def ::id int?)
(s/def ::title string?)
(s/def ::done boolean?)

(def MAX-TOASTS 5)
(def MAX-TIMEOUT 5000)
(def TIMEOUT 3)

(def default-db {:toasts (vec [])
                 :login-user nil
                 :current-route nil})

(def login-user-key "login-user")

(defn login->local-store
  [login-user]
  (.setItem js/localStorage login-user-key (str login-user)))

(defn remove->ocal-store
  []
  (.removeItem js/localStorage login-user-key))

(reg-cofx
 :local-store-user
 (fn [cofx _]
   (assoc cofx :local-store-user  
          (-> (.getItem js/localStorage login-user-key)
              (cljs.reader/read-string)))))


