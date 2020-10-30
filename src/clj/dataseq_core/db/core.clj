(ns dataseq-core.db.core
  (:require [monger.core :as mg]
            [monger.query :as q]
            [monger.collection :as mcoll]
            [monger.internal.pagination :as mp]
            [clojure.tools.logging :as log]
            [dataseq-core.config :refer [env]]))

(defn- connect
  [uri]
  (mg/connect-via-uri uri))

(defn- disconnect
  [conn]
  (mg/disconnect conn))

(def ^:private conn
  (atom nil))

(def ^:private db
  (atom nil))

(def ^:private default-collection
  (atom nil))

(defn setup-connection!
  []
  (let [conn-info (connect (:mongo-uri env))]
    (reset! conn (:conn conn-info))
    (reset! db (:db conn-info))
    (reset! default-collection (:mongo-collection env))))

(defn stop-connection!
  []
  (when @conn
    (mg/disconnect @conn))
  (reset! conn nil)
  (reset! db nil)
  (reset! default-collection nil))

(defn reset-db-conn!
  [db-name]
  (let [db-inst (mg/get-db @conn db-name)]
    (reset! db db-inst)))

(defn with-collection
  ([coll query-coll]
   (let [db-coll (if (string? coll)
                   (.getCollection @db coll)
                   coll)
         empty-query (q/empty-query db-coll)
         query (apply merge empty-query query-coll)]
     (log/info "Query: " query)
     (q/exec query)))
  ([coll] (with-collection coll [])))

(defn query
  ([query-coll]
   (with-collection @default-collection query-coll))
  ([]
   (with-collection @default-collection)))

(defn count-coll
  [query-map]
  (mcoll/count @db @default-collection query-map))

(defn find-coll
  [query]
  {:query query})

(defn fields
  [flds]
  {:fields flds})

(defn sort-coll
  [srt]
  {:sort srt})

(defn skip
  [^long n]
  {:skip n})

(defn limit
  [^long n]
  {:limit n})

(defn batch-size
  [^long n]
  {:batch-size n})

(defn paginate
  [{:keys [page per-page] :or {page 1 per-page 10}}]
  {:limit per-page :skip (mp/offset-for page per-page)})

(defn hint
  [h]
  {:hint h})

(defn snapshot
  []
  {:snapshot true})

(defn count-group-by
  ([coll query-map group-name]
   (mcoll/aggregate @db coll [{"$match" query-map}
                              {"$group" {:_id (str "$" group-name)
                                         :total {"$sum" 1}}}]
                    :cursor {:batch-size 0}))
  ([query-map group-name]
   (count-group-by @default-collection query-map group-name))
  ([group-name]
   (count-group-by @default-collection {} group-name)))
