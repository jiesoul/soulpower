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
 :tag/data
 (fn [db _]
   (get-in db [:tag :data])))

(reg-sub
 :tag/datasources
 (fn [_]
   [(subscribe [:tag/data])
    (subscribe [:tag/query])])
 (fn [[data query]]
   {:data (:list data)
    :pagination {:query query
                 :total (:total data)}}))

(reg-sub
 :tag/edit
 (fn [db _]
   (get-in db [:tag :edit])))