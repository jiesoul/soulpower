(ns admin.subs 
  (:require [re-frame.core :as re-frame]
            [admin.auth.subs]
            [admin.category.subs]
            [admin.tag.subs]
            [admin.user.subs]
            [admin.article.subs]
            [admin.article-comment.subs]))

(re-frame/reg-sub
 :debug
 (fn [db]
   (:debug db)))

(re-frame/reg-sub
 :token
 (fn [db]
   (get-in db [:login :user :token])))

(re-frame/reg-sub
 :toasts
 (fn [db]
   (get-in db [:toasts])))

(re-frame/reg-sub
 :modal
 (fn [db _]
   (get-in db [:modal])))

(re-frame/reg-sub 
 :current-route 
 (fn [db]
   (:current-route db)))

(re-frame/reg-sub
 :default-pagination
 (fn [db _]
   (get-in db [:default-pagination])))

(re-frame/reg-sub
 :current-route-edit
 (fn [db]
   (get-in db [:current-route :edit])))