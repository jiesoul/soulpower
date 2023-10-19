(ns backend.handler.category-handler-test
  (:require [backend.core-test :as core]
            [backend.category.category-handler :refer [create-category!
                                                       query-categories
                                                       get-category]]
            [clojure.test :refer [deftest is]]
            [clojure.tools.logging :as log]))

(deftest query-categories-test
  (let [body (:body (query-categories (core/env)))]
    (is (= 200 (:status body)))
    (is (coll? (:list (:data body))))))

(deftest create-category-test
  (let [category {:name "test" :pid 0}
        result (:body (create-category! (core/env)))]
    (is (= 400 (:status result)))))

(deftest create-category-romdon-test
  (let [category {:name (str "test" (rand-int 1000)) :pid 0}
        _ (log/debug "Category: " category)
        result (:body (create-category! (core/env)))]
    (is (= 200 (:status result)))))

(deftest get-category-test
  (let [id 1]
    (is (= id (get-category (core/env))))))
