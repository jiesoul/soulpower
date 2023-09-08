(ns admin.category.subs
  (:require [re-frame.core :refer [reg-sub]]))

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
 :category/edit 
 (fn [db _]
   (get-in db [:category :edit])))

(reg-sub 
 :category/pagination 
 (fn [db _]
   ))