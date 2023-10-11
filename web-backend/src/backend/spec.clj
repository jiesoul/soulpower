(ns backend.spec
  (:require [clojure.spec.alpha :as s]))

(s/def :bs/not-empty-string (s/and string? #(> (count %) 0)))
(s/def :bs/password (s/and :bs/not-empty-string #(>= (count %) 8)))
(s/def :bs/token (s/and string? #(re-matches #"^Token (.+)$" %)))
(s/def :bs/name :bs/not-empty-string)
(s/def :bs/description string?)

(s/def :bs/path-str (s/and :bs/not-empty-string #(re-matcher #"[a-z|A-Z|0-9|_|-]*" %)))

(s/def :bs/page (s/and string? #(re-matcher #"^page=(/d+)$" %)))
(s/def :bs/page-size (s/and string? #(re-matcher #"^page-size=(/d+)$" %)))
(s/def :bs/sort (s/and string? #(re-matcher #"^sort=(.*)$" %)))
(s/def :bs/filter (s/and string? #(re-matcher #"^filter=(.*)$" %)))
(s/def :bs/q (s/and string? #(re-matcher #"^q=(.*)$" %)))
(s/def :bs/query
  (s/keys :opt-un [:bs/page :bs/page-size :bs/sort :bs/filter :bs/q]))

(s/def :bs/username :bs/name)
(s/def :bs/login-user
  (s/keys :req-un [:bs/username :bs/password]))

(s/def :bs/nickname string?)
(s/def :bs/birthday inst?)
(s/def :bs/age pos-int?)
(s/def :bs/avatar string?)
(s/def :bs/phone string?)

(s/def :bs/UserProfile
  (s/keys :opt-un [:bs/nickname :bs/birthday :bs/age :bs/avatar :bs/phone]))

(s/def :bs/old-password :bs/password)
(s/def :bs/new-password :bs/password)
(s/def :bs/confirm-password :bs/password)
(s/def :bs/UpdatePassword
  (s/keys :req-un [:bs/old-password :bs/new-password :bs/confirm-password]))

(s/def :bs/Category
  (s/keys :req-un [:bs/name]
          :opt-un [:bs/description]))

(s/def :bs/Tag
  (s/keys :req-un [:bs/name]
          :opt-un [:bs/description]))

(s/def :bs/title string?)
(s/def :bs/summary string?)
(s/def :bs/content-md string?)
(s/def :bs/author string?)
(s/def :bs/detail
  (s/keys :req-un [:bs/content-md]))
(s/def :bs/Article
  (s/keys :opt-un [:bs/title :bs/summary :bs/author :bs/detail]))

(s/def :bs/category-id pos-int?)
(s/def :bs/tag-ids (s/coll-of pos-int?))
(s/def :bs/Article-Push
  (s/keys :req-un [:bs/category-id :bs/tag-ids]))

(s/def :bs/secret string?)
(s/def :bs/app-category-id string?)
(s/def :bs/App
  (s/keys :req-un [:bs/name :bs/secret :bs/app-category-id] :opt-un [:bs/description]))

(s/def :bs/id string?)
(s/def :bs/App-Category
  (s/keys :req-un [:bs/id :bs/name] :opt-un [:bs/description]))
