(ns build 
  (:refer-clojure :exclude [test])
  (:require [clojure.tools.build.api :as b]))

(def lib 'soulpower)
(def version (format "1.0.%s" (b/git-count-revs nil)))
(def main 'backend.core)
(def class-dir "target/classes")

(defn clean [_]
  (println "Cleaning...")
  (b/delete {:path "target"}))

(defn- uber-opts [opts]
  (assoc opts 
         :lib lib 
         :main main
         :uber-file (format "target/%s-%s.jar" lib version)
         :basis (b/create-basis {:project "deps.edn"
                                 :alias [:backend]})
         :class-dir class-dir
         :src-dirs ["src"]
         :ns-compile [main]))

(defn uber [{:keys [env] :as opts}]
  (clean nil)
  (let [opts (uber-opts opts)]
    (println "Copying files...")
    (b/copy-dir {:src-dirs ["prod-resources" "src"]
                 :target-dir class-dir})
    (println "Compiling files...")
    (b/compile-clj opts)
    (println "Creating uberjar...")
    (b/uber opts)))