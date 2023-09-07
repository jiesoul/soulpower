(ns admin.db
  (:require [cljs.reader]
            [cljs.spec.alpha :as s]
            [re-frame.core :refer [reg-cofx]]))

(s/def ::id int?)
(s/def ::title string?)
(s/def ::done boolean?)

(def MAX-TOASTS 5)
(def MAX-TIMEOUT 20000)

(def default-db {:toasts {}
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

(reg-cofx 
 :now 
 (fn [cofx _]
   (assoc cofx :now (js/Date.))))

(reg-cofx
 :uuid 
 (fn [cofx _]
   (assoc cofx :uuid (keyword (str (random-uuid))))))
