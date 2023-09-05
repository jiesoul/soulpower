(ns admin.auth.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
 :login-user
 (fn [db]
   (get-in db [:login :user])))

(reg-sub
 :login-status
 (fn [db]
   (get-in db [:login :status])))