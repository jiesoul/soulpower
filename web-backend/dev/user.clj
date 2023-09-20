(ns user
  (:require [backend.core :as core]
            [integrant.repl :as ig-repl]
            [ragtime.next-jdbc :as rt-jdbc]
            [ragtime.repl :as rt-repl]
            [clojure.tools.logging :as log]))

(ig-repl/set-prep! (constantly (core/system-config-start)))

(defn load-db-config []
  (let [config (:backend/hikaricp (core/read-config :dev))
        _ (log/debug "ragtime use db: " config)]
    {:datastore  (rt-jdbc/sql-database (assoc config :user (:username config)))
     :migrations (rt-jdbc/load-resources "migrations")}))

(defn migrate []
  (rt-repl/migrate (load-db-config)))

(defn rollback []
  (rt-repl/rollback (load-db-config)))

(def go ig-repl/go)
(def halt ig-repl/halt)
(def reset ig-repl/reset)
(def reset-all ig-repl/reset-all)
