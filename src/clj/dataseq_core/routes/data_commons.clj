(ns dataseq-core.routes.data-commons
  (:require [clojure.tools.logging :as log]
            [ring.util.http-response :refer [ok bad-request]]
            [dataseq-core.db.core :as dc]
            [dataseq-core.routes.data-commons-spec :as dc-spec]
            [dataseq-core.parser.core :as parser]))

(def data-commons
  [""
   {:swagger {:tags ["Data Commons"]}}

   ["/list-collections"
    {:get {:summary "Get all valid collections from schema collection."
           :parameters {}
           :responses {200 {:body {:collections any?}}}
           :handler (fn [{{:keys [_]} :parameters}]
                      (ok {:collections (dc/list-collections)}))}}]

   ["/schema/:coll-name"
    {:get {:summary "Get all fields from schema collection."
           :parameters {:path dc-spec/collection-name}
           :responses {200 {:body dc-spec/fields}}
           :handler (fn [{{:keys [path]} :parameters}]
                      (let [coll-name (:coll-name path)]
                        (ok (dc/query [(dc/find-coll {:collection coll-name})]))))}}]

   ["/collections/:coll-name"
    {:post  {:summary    "Get collections"
             :parameters {:query dc-spec/collection-params-query
                          :body any?
                          :path dc-spec/collection-name}
             :responses  {200 {:body {:total    nat-int?
                                      :page     pos-int?
                                      :per_page pos-int?
                                      :data     any?}}}
             :handler    (fn [{{:keys [query body path]} :parameters}]
                           (let [page (if (:page query) (:page query) 1)
                                 per-page (if (:per_page query) (:per_page query) 10)
                                 results (parser/check-rules body)
                                 coll-name (:coll-name path)]
                             (if results (bad-request results)
                                 (let [query-map (parser/parse body)]
                                   (log/info "Query collections: " query-map query)
                                   (ok {:total (dc/count-coll coll-name query-map)
                                        :page page
                                        :per_page per-page
                                        :data (dc/query coll-name [(dc/find-coll query-map)
                                                                   (dc/paginate {:page page :per-page per-page})])})))))}}]
   ["/count-collections/:coll-name"
    {:post  {:summary    "Get counts by group"
             :parameters {:query dc-spec/count-params-query
                          :body any?
                          :path dc-spec/collection-name}
             :responses  {200 {:body any?}}
             :handler    (fn [{{:keys [query body path]} :parameters}]
                           (let [group (:group query)
                                 dedup-field (:dedup_field query)
                                 results (parser/check-rules body)
                                 coll-name (:coll-name path)]
                             (if results (bad-request results)
                                 (let [query-map (parser/parse body)]
                                   (log/info "Count collections: " query-map group query)
                                   (if dedup-field
                                     (ok (dc/count-group-by coll-name query-map group))
                                     (ok (dc/count-group-uniq-by coll-name query-map group dedup-field)))))))}}]])