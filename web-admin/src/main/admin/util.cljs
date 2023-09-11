(ns admin.util
  (:require [cljs.pprint]
            [clojure.string :as str]
            [reitit.frontend.easy :as rfe]
            ["moment" :as moment]))

;; (def dtf (java.time.format.DateTimeFormatter/ofPattern "yyyyMMddHHmmssSSSSS"))

(defn href
  "Return relative url for given route. Url can be used in HTML links"
  ([k] (href k nil nil))
  ([k params] (href k params nil))
  ([k params query]
   (rfe/href k params query)))

(defn get-value [d]
  (-> d .-target .-value))

(defn get-trim-value [d]
  (let [v (str/trim (get-value d))]
    (if (or (nil? v) (str/blank? v)) nil v)))

(defn get-filter-like [n e]
  (str n " lk '" (get-trim-value e) "'"))

(defn blank? [v]
  (or (nil? v) (str/blank? v)))

(defn valid?
  [[_ v]]
  (and (string? v) (seq v)))

(defn save!
  [a k]
  #(swap! a assoc k (-> % .-target .-value)))

(defn input [label k type state]
  [:div.flex.flex.flex-wrap.gap-2.itemc-center.mt-1
   [:label {:htmpFor (name k) :className "login-label"} label]
   [:div 
    [:input {:type type
             :id (name k)
             :name (name k)
             :className "login-input"
             :placeholder (name k)
             :value (k @state)
             :on-change (save! state k)
             :required true}]]])

(defn clog
  ([msg] (clog msg nil))
  ([msg data] 
   (let [buf (if data 
               (str msg ": " data)
               msg)]
     (js/console.log buf))))

(defn error-message
  [title msg]
  [:<>
   [:div.bg-red-100.border.border-red-400.text-red-700.px-4.py-3.rounded.relative
    {:role "alert"}
    [:strong.font-bold.mr-2 title]
    [:span.block.sm:inline msg]]])

(defn my-parseInt
  [s]
  (js/parseInt s 10))

(defn format-time [time]
  (if-not time
    ""
    (.format (moment time) "YYYY-MM-DD HH:mm:ss")))

(def page-show-count 4)

(defn gen-pagination [{:keys [total query] :as pagination}]
  (let [page-size (:page-size query)
        page (:page query)
        total-pages (quot (dec (+ total page-size)) page-size)
        start (inc (* (dec page) page-size))
        end (dec (+ start page-size))
        prev-page (if (<= page 1) 1 (dec page))
        next-page (if (< page total-pages) (inc page) total-pages)
        page-no page-show-count
        show-pages (range (max 1 (- page page-no)) (inc (min (+ page-no page) total-pages)))]
    (merge pagination {:page-size page-size 
                       :page page
                       :total-pages total-pages
                       :start start
                       :end end
                       :prev-page prev-page
                       :next-page next-page
                       :show-pages show-pages})))