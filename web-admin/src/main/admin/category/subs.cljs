(ns admin.category.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub 
 :category 
 (fn [db _]
   (get-in db [:category])))

(reg-sub
 :category/query
 (fn [db _]
   (get-in db [:category :query])))

(reg-sub
 :category/data
 (fn [db _]
   (get-in db [:category :data])))

(reg-sub 
 :category/datasources
 (fn [_]
   [(subscribe [:category/data])
    (subscribe [:category/query])])
 (fn [[data query]]
   {:data (:list data)
    :pagination {:query query
                 :total (:total data)}}))

(reg-sub 
 :category/edit 
 (fn [db _]
   (get-in db [:category :edit])))