(ns admin.user.views
  (:require [admin.shared.layout :refer [layout-admin]]))

(defn index
  []
  [layout-admin
   [:<>]])