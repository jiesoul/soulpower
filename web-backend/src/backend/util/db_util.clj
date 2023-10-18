(ns backend.util.db-util
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [next.jdbc.prepare :as p]
            [next.jdbc.result-set :as rs])
  (:import [java.sql PreparedStatement]))

(extend-protocol rs/ReadableColumn
  java.sql.Date
  (read-column-by-label [^java.sql.Date v _]
    (.toLocalDate v))
  (read-column-by-index [^java.sql.Date v _2 _3]
    (.toLocalDate v))
  java.sql.Timestamp
  (read-column-by-label [^java.sql.Timestamp v _]
    (.toInstant v))
  (read-column-by-index [^java.sql.Timestamp v _2 _3]
    (.toInstant v)))

(extend-protocol p/SettableParameter
  java.time.Instant
  (set-parameter [^java.time.Instant v ^PreparedStatement ps ^long i]
    (.setTimestamp ps i (java.sql.Timestamp/from v)))
  java.time.LocalDate
  (set-parameter [^java.time.LocalDate v ^PreparedStatement ps ^long i]
    (.setTimestamp ps i (java.sql.Timestamp/valueOf (.atStartOfDay v))))
  java.time.LocalDateTime
  (set-parameter [^java.time.LocalDateTime v ^PreparedStatement ps ^long i]
    (.setTimestamp ps i (java.sql.Timestamp/valueOf v))))

(defn my-sql-logger [sym sql-params]
  (log/debug "sql-ps" sym sql-params))

(defn my-result-logger [sym sql-rs]
  (log/debug "sql-rs: " sym sql-rs))

(defn populate
  [_ db-type]
  (let [auto-key (if (= "sqlite" db-type)
                   "primary key autoincrement"
                   (str " generated always as identity "
                        " (start with 1, increment by 1) "
                        " primary key "))]
    auto-key))

(defn del-qu [s]
  (if (and (str/starts-with? s "'") (str/ends-with? s "'"))
    (let [end (dec (count s))]
      (subs s 1 end))
    s))

(defn not-blank? [s]
  (if (or (nil? s) (str/blank? (str/trim s))) nil (str/trim s)))

(defn- op-convert
  [s]
  (loop [sql s
         w ""
         v []]
    (if (seq sql)
      (let [fst (first sql)
            snd (second sql)
            [ssql ww vv] (case fst
                           "eq" [(nnext sql) (str w " = ?  ") (conj v (del-qu snd))]
                           "lk" [(nnext sql) (str w " like ? ") (conj v (str "%" (del-qu snd) "%"))]
                           "ne" [(nnext sql) (str w " != ?") (conj v snd)]
                           "gt" [(nnext sql) (str w " > ? ") (conj v snd)]
                           "ge" [(nnext sql) (str w " >= ? ") (conj v snd)]
                           "lt" [(nnext sql) (str w " < ? ") (conj v snd)]
                           "le" [(nnext sql) (str w " <= ? ") (conj v snd)]
                           [(next sql) (str w " " fst " ") v])]
        (recur ssql ww vv))
      [w v])))

(defn- filter->map [filter-str]
  (-> filter-str
      (str/split #" +")
      op-convert))

(defn filter->sql [filter-str]
  (if (str/blank? filter-str)
    ["" []]
    (let [[s v] (filter->map filter-str)]
      [(str " where " s) v])))

(defn page->sql [page page-size]
  [" limit ? offset ? " [page-size (* (dec page) page-size)]])

(defn sort->sql [sort]
  (if sort
    [(str " order by " sort) []]
    ["" []]))

(defn query->sql
  ([query qsql] (query->sql query qsql nil))
  ([{:keys [page page-size sort filter] :as query} qsql tsql]
   (let [_ (log/debug "Query: " query)
         _ (log/debug "query sql: " qsql)
         _ (log/debug "Total sql: " tsql)
         [ps pv] (page->sql page page-size)
         [ss _] (sort->sql sort)
         _ (log/debug "sort: " ss)
         [fs fv] (filter->sql filter)
         _ (log/debug "Where: " fs fv)
         rs [(into [(str/join " " [qsql fs ss ps])] (into fv pv))
             (if tsql (into [(str/join " " [tsql fs])] fv) tsql)]
         _ (log/debug "Result: " rs)
         _ (log/debug "Result: " rs)]
     rs)))
