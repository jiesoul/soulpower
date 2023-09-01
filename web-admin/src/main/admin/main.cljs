(ns admin.main
  (:require [day8.re-frame.http-fx]
            [admin.views.article :as article]
            [admin.views.article-comment :as article-comment]
            [admin.views.category :as category]
            [admin.views.dashboard :as dashboard]
            [admin.views.user-token :as user-token]
            [admin.views.login :as f-login]
            [admin.views.tag :as tag]
            [admin.views.user :as user]
            [admin.shared.toasts :as toasts]
            [admin.state :as f-state]
            [admin.util :as f-util]
            [re-frame.core :as re-frame]
            [re-frame.db]
            [reagent.dom :as rdom]
            [reitit.coercion.spec :as rss]
            [reitit.frontend :as rf]
            [reitit.frontend.controllers :as rfc]
            [reitit.frontend.easy :as rfe]))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   {:current-route nil
    :toasts (vec [])
    :debug true
    :login {:status nil}}))

(re-frame/reg-event-fx
 ::f-state/load-localstore
 (fn [cofx _]
   (let [defaults (:local-store cofx)]
     {:db (assoc (:db cofx) :defaults defaults)})))

(re-frame/reg-event-fx
 ::f-state/req-failed-message
 (fn [{:keys [db]} [_ {:keys [response]}]]
   (f-util/clog "resp failed: " response)
   {:db db
    :fx [[:dispatch [::toasts/push {:content (:message response)
                                    :type :error}]]]}))

(re-frame/reg-event-fx
 ::f-state/navigate
 (fn [_ [_ & route]]
   {::navigate! route}))

(re-frame/reg-event-db
 ::f-state/navigated
 (fn [db [_ new-match]]
   (let [old-match (:current-route db)
         new-path (:path new-match)
         controllers (rfc/apply-controllers (:controllers old-match) new-match)]
     (js/console.log (str "new-path: " new-path))
     (cond-> (assoc db :current-route (assoc new-match :controllers controllers))
       (= "/" new-match) (-> (assoc :login-status nil)
                             (assoc :user nil))))))

(re-frame/reg-fx
 ::navigate!
 (fn [route]
   (apply rfe/push-state route)))

(re-frame/reg-event-db
 ::f-state/init-current-route-result
 (fn [db [_ data]]
   (assoc-in db [:current-route :result] data)))

(re-frame/reg-event-db
 ::f-state/init-current-route-edit
 (fn [db [_ current]]
   (assoc-in db [:current-route :edit] current)))

(re-frame/reg-event-db
 ::f-state/clean-current-route-edit
 (fn [db [_ _]]
   (assoc-in db [:current-route :edit] nil)))

(def routes
  ["/"
  
   ;; admin page
   ["" {:name ::f-state/login
             :view f-login/login
             :link-text "Login"
             :controllers [{:start (fn [& params] (js/console.log (str "Entering login, params: " params)))
                            :stop (fn [& params] (js/console.log (str "Leaving login, params: " params)))}]}]

   ["dashboard" {:name ::f-state/dashboard
                 :view dashboard/index
                 :link-text "Dashboard"
                 :controllers [{:start (fn [& params] 
                                         (js/console.log (str "Entering dashboard, params: " params)))
                                :stop (fn [& params] (js/console.log (str "Leaving login, params: " params)))}]}]

   ["categories" {:name ::f-state/categories
                  :view category/index
                  :link-text "Categories"
                  :controllers [{:start (fn [& params] 
                                        
                                          (js/console.log (str "Entering categories, params: " params)))
                                 :stop (fn [& params] 
                                         
                                         (js/console.log (str "Leaving categories, params: " params)))}]}]

   ["tags" {:name ::f-state/tags
            :view tag/index
            :link-text "Tags"
            :controllers [{:start (fn [& params] 
                                    (js/console.log (str "Entering tags, params: " params)))
                           :stop (fn [& params] 
                                   (js/console.log (str "Leaving tags, params: " params)))}]}] 
   
   ["articles" {:name ::f-state/articles
                :view article/index
                :link-text "Articles"
                :controllers [{:start (fn [& params] 
                                        (re-frame/dispatch [::category/get-all-categories])
                                        (js/console.log (str "Entering articles, params: " params)))
                               :stop (fn [& params] (js/console.log (str "Leaving articles, params: " params)))}]}]
   
   ["articles-comments" {:name ::f-state/articles-comments
                         :view article-comment/index
                         :link-text "Articles-Comments"
                         :controllers [{:start (fn [& params] (js/console.log (str "Entering dashboard, params: " params)))
                                        :stop (fn [& params] (js/console.log (str "Leaving login, params: " params)))}]}]
   
   
   ["users" {:name ::f-state/users
             :view user/index
             :link-text "Users"
             :controllers [{:start (fn [& params] (js/console.log (str "Entering dashboard, params: " params)))
                            :stop (fn [& params] (js/console.log (str "Leaving login, params: " params)))}]}]
   
   ["user-token" {:name ::f-state/user-tokens
                  :view user-token/index
                  :link-text "Users Tokens"
                  :controllers [{:start (fn [& params] (js/console.log (str "Entering dashboard, params: " params)))
                                 :stop (fn [& params] (js/console.log (str "Leaving login, params: " params)))}]}]])

(defn on-navigate [new-match]
  (f-util/clog "on-navigate, new-match" new-match)
  (when new-match
    (re-frame/dispatch [::f-state/navigated new-match])))

(def router
  (rf/router
   routes
   {:data {:coercion rss/coercion}}))

(defn init-routes! []
  (js/console.log "initializing routes")
  (rfe/start!
   router
   on-navigate
   {:user-fragment true}))

(defn router-component [_]
  (f-util/clog "Enter router-component")
  (let [current-route @(re-frame/subscribe [::f-state/current-route])
        path-params (:path-params current-route)
        _ (f-util/clog "router-component, path-params" path-params)]
    [:div
     (when current-route
       [(-> current-route :data :view) current-route])]))


;; Setup

(def debug? ^boolean goog.DEBUG)

(defn dev-setup []
  (when debug?
    (enable-console-print!)
    (println "dev mode")))

(defn ^:dev/after-load start []
  (js/console.log "Enter start")
  (re-frame/clear-subscription-cache!)
  (init-routes!)
  (rdom/render [router-component {:router router}]
               (.getElementById js/document "root")))

(defn ^:export init! []
  (js/console.log "Enter init!")
  (re-frame/dispatch-sync [::initialize-db])
  ;; (dev-tools/start! {:state-atom re-frame.db/app-db})
  (dev-setup)
  (start))

(defn ^:dev/after-load reload []
  (.reload router))
