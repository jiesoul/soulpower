(ns admin.main
  (:require [admin.events]
            [admin.views :as views]
            [admin.article.views :as article]
            [admin.article-comment.views :as article-comment]
            [admin.category.views :as category]
            [admin.dashboard.views :as dashboard]
            [admin.auth.views :as auth]
            [admin.tag.views :as tag]
            [admin.user.views :as user]
            [admin.util :as util]
            [re-frame.core :as re-frame]
            [reagent.dom :as rdom]
            [reitit.coercion.spec :as rss]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]))

(def routes
  ["/"
   
   ["" {:name ::views/login
        :view auth/login
        :link-text "Login"
        :controllers [{:start (fn [& params] (js/console.log (str "Entering login, params: " params)))
                       :stop (fn [& params] (js/console.log (str "Leaving login, params: " params)))}]}]

   ["dashboard" {:name ::views/dashboard
                 :view dashboard/index
                 :link-text "Dashboard"
                 :controllers [{:start (fn [& params] 
                                         (js/console.log (str "Entering dashboard, params: " params)))
                                :stop (fn [& params] (js/console.log (str "Leaving login, params: " params)))}]}]

   ["categories" {:name ::views/categories
                  :view category/index
                  :link-text "Categories"
                  :controllers [{:start (fn [& params] 
                                        
                                          (js/console.log (str "Entering categories, params: " params)))
                                 :stop (fn [& params] 
                                         
                                         (js/console.log (str "Leaving categories, params: " params)))}]}]

   ["tags" {:name ::views/tags
            :view tag/index
            :link-text "Tags"
            :controllers [{:start (fn [& params] 
                                    (js/console.log (str "Entering tags, params: " params)))
                           :stop (fn [& params] 
                                   (js/console.log (str "Leaving tags, params: " params)))}]}] 
   
   ["articles" {:name ::views/articles
                :view article/index
                :link-text "Articles"
                :controllers [{:start (fn [& params] 
                                        (re-frame/dispatch [:get-all-categories])
                                        (js/console.log (str "Entering articles, params: " params)))
                               :stop (fn [& params] (js/console.log (str "Leaving articles, params: " params)))}]}]
   
   ["articles-comments" {:name ::views/articles-comments
                         :view article-comment/index
                         :link-text "Articles-Comments"
                         :controllers [{:start (fn [& params] (js/console.log (str "Entering dashboard, params: " params)))
                                        :stop (fn [& params] (js/console.log (str "Leaving login, params: " params)))}]}]
   
   
   ["users" {:name ::views/users
             :view user/index
             :link-text "Users"
             :controllers [{:start (fn [& params] (js/console.log (str "Entering dashboard, params: " params)))
                            :stop (fn [& params] (js/console.log (str "Leaving login, params: " params)))}]}]])

(def router
  (rf/router
   routes
   {:data {:coercion rss/coercion}}))

(defn on-navigate [new-match]
  (util/clog "on-navigate, new-match" new-match)
  (when new-match
    (re-frame/dispatch [:navigated new-match])))

(defn init-routes! []
  (js/console.log "initializing routes")
  (rfe/start!
   router
   on-navigate
   {:user-fragment true}))

(defn router-component [_]
  (util/clog "Enter router-component")
  (let [current-route @(re-frame/subscribe [:current-route])
        path-params (:path-params current-route)
        _ (util/clog "router-component, path-params" path-params)]
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
  
  
  )

(defn ^:export init! []
  (js/console.log "Enter init!")
  (re-frame/clear-subscription-cache!)
  (re-frame/dispatch-sync [:initialize-db])
  (dev-setup)
  (init-routes!)
  (rdom/render [router-component {:router router}]
               (.getElementById js/document "root")))

(defn ^:dev/after-load reload []
  (.reload router))
