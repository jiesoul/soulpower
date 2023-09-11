(ns admin.article-comment.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub
 :article-comment
 (fn [db _]
   (get-in db [:article-comment])))

(reg-sub
 :article-comment/query
 (fn [db _]
   (get-in db [:article-comment :query])))

(reg-sub
 :article-comment/data
 (fn [db _]
   (get-in db [:article-comment :data])))

(reg-sub
 :article-comment/datasources
 (fn [_]
   [(subscribe [:article-comment/data])
    (subscribe [:article-comment/query])])
 (fn [[data query]]
   {:data (:list data)
    :pagination {:query query
                 :total (:total data)}}))

(reg-sub
 :article-comment/edit
 (fn [db _]
   (get-in db [:article-comment :edit])))