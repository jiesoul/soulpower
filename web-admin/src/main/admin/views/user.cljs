(ns admin.views.user 
  (:require [admin.shared.layout :refer [layout-dash]]))

(defn index
  []
  [layout-dash
   [:<>]])