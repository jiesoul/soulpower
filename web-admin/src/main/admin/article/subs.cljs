(ns admin.article.subs 
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub
 :article
 (fn [db _]
   (get-in db [:article])))

(reg-sub
 :article/query
 (fn [db _]
   (get-in db [:article :query])))

(reg-sub
 :article/data
 (fn [db _]
   (get-in db [:article :data])))

(reg-sub
 :article/datasources
 (fn [_]
   [(subscribe [:article/data])
    (subscribe [:article/query])])
 (fn [[data query]]
   {:data (:list data)
    :pagination {:query query
                 :total (:total data)}}))

(reg-sub
 :article/edit
 (fn [db _]
   (get-in db [:article :edit])))