(ns admin.user.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub
 :user
 (fn [db _]
   (get-in db [:user])))

(reg-sub
 :user/query
 (fn [db _]
   (get-in db [:user :query])))

(reg-sub
 :user/data
 (fn [db _]
   (get-in db [:user :data])))

(reg-sub
 :user/datasources
 (fn [_]
   [(subscribe [:user/data])
    (subscribe [:user/query])])
 (fn [[data query]]
   {:data (:list data)
    :pagination {:query query
                 :total (:total data)}}))

(reg-sub
 :user/edit
 (fn [db _]
   (get-in db [:user :edit])))