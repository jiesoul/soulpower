(ns admin.views.user 
  (:require [admin.shared.layout :refer [layout-admin]]))

(defn index
  []
  [layout-admin
   [:<>]])