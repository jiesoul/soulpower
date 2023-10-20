(ns admin.tag.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub
 :tag
 (fn [db _]
   (get-in db [:tag])))

(reg-sub
 :tag/query
 (fn [db _]
   (get-in db [:tag :query])))

(reg-sub
 :tag/list
 (fn [db _]
   (get-in db [:tag :list])))

(reg-sub
 :tag/total
 (fn [db _]
   (get-in db [:tag :total])))

(reg-sub
 :tag/datasources
 (fn [_]
   [(subscribe [:tag/list])
    (subscribe [:tag/total])
    (subscribe [:tag/query])])
 (fn [[list total query]]
   {:data list
    :pagination {:query query
                 :total total}}))

(reg-sub
 :tag/edit
 (fn [db _]
   (get-in db [:tag :edit])))
