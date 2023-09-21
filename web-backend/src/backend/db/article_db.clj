(ns backend.db.article-db
  (:require [backend.db.article-tag-db :as article-tag-db]
            [backend.db.comment-db :as comment-db]
            [backend.util.db-util :as du]
            [clojure.tools.logging :as log]
            [next.jdbc :as jdbc :refer [unqualified-snake-kebab-opts]]
            [next.jdbc.result-set :as rs]
            [next.jdbc.sql :as sql]))

(defn query [db opts]
  (let [[ws wv] (du/opt-to-sql opts)
        ss (du/opt-to-sort opts)
        [ps pv] (du/opt-to-page opts)
        q-sql (into [(str "select * from article " ws ss ps)] (into wv pv))
        articles (sql/query db q-sql {:builder-fn rs/as-unqualified-kebab-maps})
        t-sql (into [(str "select count(1) as c from article " ws)] wv)
        total (:c (first (sql/query db t-sql)))]
    {:list articles
     :total total}))

(defn create! [db {:keys [id detail] :as article}]
  (jdbc/with-transaction [tx db]
    (sql/insert! tx :article_detail (assoc detail :article-id id) unqualified-snake-kebab-opts)
    (sql/insert! tx :article (dissoc article :detail) unqualified-snake-kebab-opts)))

(defn get-detail-by-article-id [db article-id]
  (first (sql/find-by-keys db :article_detail {:article_id article-id} {:builder-fn rs/as-unqualified-kebab-maps})))

(defn get-article-by-id [db id]
  (jdbc/with-transaction [tx db]
    (let [article (sql/get-by-id tx :article id {:builder-fn rs/as-unqualified-kebab-maps})]
      article)))

(defn get-article-and-detail-by-id [db id]
  (jdbc/with-transaction [tx db]
    (let [article (sql/get-by-id tx :article id {:builder-fn rs/as-unqualified-kebab-maps})
          detail (get-detail-by-article-id tx id)]
      (assoc article :detail detail))))

(defn update! [db id {:keys [detail] :as article}] 
  (jdbc/with-transaction [tx db]
    (sql/update! tx :article_detail detail {:article_id id} unqualified-snake-kebab-opts)
    (sql/update! tx :article (dissoc article :detail) {:id id} unqualified-snake-kebab-opts)))

(defn delete! [db id]
  (jdbc/with-transaction [tx db]
    (comment-db/delete-by-article-id! tx id)
    (sql/delete! tx :article_detail {:article_id id})
    (sql/delete! tx :article {:id id})))

(defn push! [db id {:keys [tag-ids] :as article}]
  (jdbc/with-transaction [tx db]
    (article-tag-db/delete-by-article-id tx id)
    (when-not (empty? tag-ids)
      (article-tag-db/create-multi! tx id tag-ids))
    (sql/update! tx :article (dissoc article :tag-ids) {:id id} unqualified-snake-kebab-opts)))

(defn get-pushed [db opts]
  (let [[ps pv] (du/opt-to-page opts)
        q-sql (into ["select * from article where push_flag = 1 order by id desc limit ? offset ? "] pv)
        _ (log/debug "query sql: " q-sql)
        articles (sql/query db q-sql {:builder-fn rs/as-unqualified-kebab-maps})
        t-sql ["select count(1) as c from article where push_flag = 1"]
        total (:c (first (sql/query db t-sql)))]
    {:list articles 
     :total total}))

(defn update-article-comment-count! [db article-id c] 
  (jdbc/execute-one! db ["update article set comment_count = comment_count + ? where id = ?" c article-id]))

(defn update-article-like-count! [db article-id c]
  (jdbc/execute! db ["update article set like_count = like_count + ? where id = ? " c article-id]))

(defn update-article-read-count! [db article-id c]
  (jdbc/execute! db ["update article set read_count = read_count + ? where id = ? " c article-id]))

(defn get-pushed-by-year [db year]
  (sql/query db ["SELECT * from article a where push_flag = 1 and date_part('year', create_time) = ? order by id desc" year]
             {:builder-fn rs/as-unqualified-kebab-maps}))

(defn get-archive [db]
  (let [years-sql "select year,c from (SELECT date_part('year', create_time) as year, count(1) :as c from article where push_flag = 1) t group by year desc"
        rs (sql/query db [years-sql] {:builder-fn rs/as-unqualified-kebab-maps})
        years (-> rs first)
        _ (log/debug "article archive years: " years)]
    years))
