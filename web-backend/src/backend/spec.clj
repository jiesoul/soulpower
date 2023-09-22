(ns backend.spec 
  (:require [clojure.spec.alpha :as s]))

(s/def :bs/not-empty-string (s/and string? #(> (count %) 0)))
(s/def :bs/password (s/and :bs/not-empty-string #(>= (count %) 8)))
(s/def :bs/token (s/and string? #(re-matches #"^Token (.+)$" %)))

(s/def :bs/path-str (s/and :bs/not-empty-string #(re-matcher #"[a-z|A-Z|0-9|_|-]*" %)))

(s/def :bs/page (s/and string? #(re-matcher #"^page=(/d+)$" %)))
(s/def :bs/page-size (s/and string? #(re-matcher #"^page-size=(/d+)$" %)))
(s/def :bs/sort (s/and string? #(re-matcher #"^sort=(.*)$" %)))
(s/def :bs/filter (s/and string? #(re-matcher #"^filter=(.*)$" %)))
(s/def :bs/q (s/and string? #(re-matcher #"^q=(.*)$" %)))
(s/def :bs/query
  (s/keys :opt-un [:bs/page :bs/page-size :bs/sort :bs/filter :bs/q]))


(s/def :bs/username :bs/not-empty-string)
(s/def :bs/login-user 
  (s/keys :req-un [:bs/username :bs/password]))