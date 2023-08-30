(ns backend.core-test
  (:require [integrant.repl :as ig-repl]
            [integrant.repl.state :as state]
            [backend.core :as core]))

(def test-config (core/system-config-start "test"))

(ig-repl/set-prep! (constantly test-config))

(defn system [] (or state/system (throw (ex-info "System not running" {}))))

(defn env [] (:backend/env (system)))

;; (defn profile [] (:backend/env (system)))

(ig-repl/go)

