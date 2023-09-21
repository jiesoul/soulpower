(ns backend.util.common-utils 
  (:require [clojure.string :as str]))

(defn str->timestamp [s]
  )

(defn gen-app-id []
  (str/replace (random-uuid) #"-" ""))