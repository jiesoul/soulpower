(ns admin.main
  (:require [admin.article-comment.views :as article-comment]
            [admin.article.views :as article]
            [admin.auth.views :as auth]
            [admin.category.views :as category]
            [admin.dashboard.views :as dashboard]
            [admin.events]
            [admin.subs :as views]
            [admin.tag.views :as tag]
            [admin.user.views :as user]
            [admin.util :as util]
            [day8.re-frame.http-fx]
            [re-frame.core :as re-frame]
            [reagent.dom :as rdom]
            [reitit.coercion.spec :as rss]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]))

(def routes
  ["/"

   ["login" {:name ::login
             :view auth/login
             :link-text "Login"
             :controllers [{:start (fn [& params]
                                     (js/console.log (str "Entering login, params: " params)))
                            :stop (fn [& params]
                                    (js/console.log (str "Leaving login, params: " params)))}]}]

   ["" {:name ::views/dashboard
        :view dashboard/index
        :link-text "Dashboard"
        :controllers [{:start (fn [& params]
                                (js/console.log (str "Entering Dashboard, params: " params)))
                       :stop (fn [& params]
                               (js/console.log (str "Leaving Dashboard, params: " params)))}]}]

   ["category" {:name ::views/category
                :view category/index
                :link-text "Category"
                :controllers [{:start (fn [& params]
                                        (re-frame/dispatch [:init-category])
                                        (js/console.log (str "Entering Category, params: " params)))
                               :stop (fn [& params]

                                       (js/console.log (str "Leaving Category, params: " params)))}]}]

   ["tag" {:name ::views/tag
           :view tag/index
           :link-text "Tag"
           :controllers [{:start (fn [& params]
                                   (re-frame/dispatch [:init-tag])
                                   (js/console.log (str "Entering Tag, params: " params)))
                          :stop (fn [& params]
                                  (js/console.log (str "Leaving Tag, params: " params)))}]}]

   ["article" {:name ::views/article
               :view article/index
               :link-text "Article"
               :controllers [{:start (fn [& params]
                                       (re-frame/dispatch [:query-categories {}])
                                       (js/console.log (str "Entering Article, params: " params)))
                              :stop (fn [& params] (js/console.log (str "Leaving Article, params: " params)))}]}]

   ["article-comment" {:name ::views/article-comment
                       :view article-comment/index
                       :link-text "Articles Comments"
                       :controllers [{:start (fn [& params]
                                               (re-frame/dispatch [:init-article-comment])
                                               (js/console.log (str "Entering Articles Comments, params: " params)))
                                      :stop (fn [& params] (js/console.log (str "Leaving Articles Comments, params: " params)))}]}]

   ["user" {:name ::views/user
            :view user/index
            :link-text "User"
            :controllers [{:start (fn [& params]
                                    (js/console.log (str "Entering User, params: " params)))
                           :stop (fn [& params] (js/console.log (str "Leaving User, params: " params)))}]}]

   ["user/profile" {:name ::views/user-profile
                    :view user/profile-page
                    :link-text "User Profile"
                    :controllers [{:start (fn [& params]
                                            (js/console.log (str "Entering User Profile, params: " params)))
                                   :stop (fn [& params] (js/console.log (str "Leaving User Profile, params: " params)))}]}]

   ["user/password" {:name ::views/user-change-password
                     :view user/password-page
                     :link-text "Change Password"
                     :controllers [{:start (fn [& params]
                                             (js/console.log (str "Entering Change Password, params: " params)))
                                    :stop (fn [& params]
                                            (js/console.log (str "Leaving Change Password, params: " params)))}]}]])

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
  (js/console.log "Enter start"))

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
