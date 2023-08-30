(ns backend.handler.user-handler-test 
  (:require [clojure.test :refer [deftest is]]
            [backend.core-test :as core]
            [backend.handler.user-handler :refer [query-users]]))

(deftest query-users-test 
  (let [body (:body (query-users (core/env) {}))
        data (:data body)]
    (is (= 200 (:status body)))
    (is (coll? data))
    (is (= 1 (:total data)))))