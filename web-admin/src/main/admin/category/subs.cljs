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
 :category/list
 (fn [db _]
   (get-in db [:category :list])))

(reg-sub
 :category/total
 (fn [db _]
   (get-in db [:category :total])))

(reg-sub
 :category/datasources
 (fn [_]
   [(subscribe [:category/list])
    (subscribe [:category/total])
    (subscribe [:category/query])])
 (fn [[list total query]]
   {:data list
    :pagination {:query query
                 :total total}}))

(reg-sub
 :category/edit
 (fn [db _]
   (get-in db [:category :edit])))
