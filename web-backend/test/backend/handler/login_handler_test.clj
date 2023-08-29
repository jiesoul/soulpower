(ns backend.handler.login-handler-test 
  (:require [clojure.test :refer [deftest is]]
            [backend.core-test :as core]
            [backend.handler.login-handler :refer [login-auth logout]]))

(deftest login-auth-test
  (is (= (:status (login-auth (core/env) "jiesoul" "12345678"))
         200)))

(deftest logout-test 
  (is (= (:status   (logout (core/env) ""))
         200)))