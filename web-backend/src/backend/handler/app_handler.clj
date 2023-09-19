(ns backend.handler.app-handler 
  (:require [backend.db.app-db :as user-token-db]
            [backend.util.resp-util :as resp-util]
            [clojure.tools.logging :as log]))

(defn query-users-tokens [env query]
  (log/debug "query users request params: "  query)
  (let [db (:db env)
        result (user-token-db/query-users-tokens db query)]
    (resp-util/response result)))

(defn create! [db app]
  )