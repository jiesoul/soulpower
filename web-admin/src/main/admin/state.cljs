(ns admin.state 
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub 
 ::current-route 
 (fn [db]
   (:current-route db)))

(re-frame/reg-sub
 ::current-route-result
 (fn [db]
   (get-in db [:current-route :result])))

(re-frame/reg-sub
 ::current-route-edit
 (fn [db]
   (get-in db [:current-route :edit])))

(re-frame/reg-sub
 ::current-modal-back?
 (fn [db]
   (get-in db [:current-route :modal :back])))

(re-frame/reg-sub
 ::current-new-modal?
 (fn [db _]
   (get-in db [:current-route :modal :new-modal?])))

(re-frame/reg-sub
 ::current-edit-modal?
 (fn [db _]
   (get-in db [:current-route :modal :edit-modal?])))

(re-frame/reg-sub
 ::current-delete-modal?
 (fn [db _]
   (get-in db [:current-route :modal :delete-modal?])))

(re-frame/reg-sub
 ::current-push-modal?
 (fn [db _]
   (get-in db [:current-route :modal :push-modal?])))

(re-frame/reg-sub
 ::token
 (fn [db]
   (get-in db [:login :user :token])))

(re-frame/reg-sub
 ::toasts
 (fn [db]
   (get-in db [:toasts])))

(re-frame/reg-sub
 ::login-user
 (fn [db]
   (get-in db [:login :user])))

(re-frame/reg-sub
 ::login-status
 (fn [db]
   (get-in db [:login :status])))

(re-frame/reg-sub
 ::debug
 (fn [db]
   (:debug db)))


(re-frame/reg-sub
 ::current-route-categories
 (fn [db _]
   (get-in db [:current-route :categories])))
