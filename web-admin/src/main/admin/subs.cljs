(ns admin.subs 
  (:require [re-frame.core :as re-frame]
            [admin.auth.subs]))

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
 :current-route 
 (fn [db]
   (:current-route db)))

(re-frame/reg-sub
 :current-route-modal
 (fn [db _]
   (get-in db [:current-route :modal])))

(re-frame/reg-sub
 :current-route-edit
 (fn [db]
   (get-in db [:current-route :edit])))

(re-frame/reg-sub 
 :current-route-query
 (fn [db]
   (get-in db [:current-route :query])))

(re-frame/reg-sub 
 :current-route-datasources
 (fn [db]
   (get-in db [:current-route :datasources])))

(re-frame/reg-sub
 :current-route-result
 (fn [db]
   (get-in db [:current-route :result])))



