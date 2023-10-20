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
   (get-in db [:article :list])))

(reg-sub
 :article/total
 (fn [db _]
   (get-in db [:article :total])))

(reg-sub
 :article/datasources
 (fn [_]
   [(subscribe [:article/data])
    (subscribe [:article/total])
    (subscribe [:article/query])])
 (fn [[data total query]]
   {:data data
    :pagination {:query query
                 :total total}}))

(reg-sub
 :article/edit
 (fn [db _]
   (get-in db [:article :edit])))

(reg-sub
 :article/categories
 (fn [_] [(subscribe [:category/list])])
 (fn [[categories]]
   categories))
