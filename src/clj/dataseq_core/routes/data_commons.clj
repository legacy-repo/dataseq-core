(ns dataseq-core.routes.data-commons
  (:require [clojure.tools.logging :as log]
            [ring.util.http-response :refer [ok bad-request]]
            [dataseq-core.db.core :as dc]
            [dataseq-core.routes.data-commons-spec :as dc-spec]
            [dataseq-core.parser.core :as parser]))

(def data-commons
  [""
   {:swagger {:tags ["Data Commons"]}}

   ["/collections"
    {:post  {:summary    "Get collections"
             :parameters {:query dc-spec/collection-params-query :body any?}
             :responses  {200 {:body {:total    nat-int?
                                      :page     pos-int?
                                      :per_page pos-int?
                                      :data     any?}}}
             :handler    (fn [{{:keys [query body]} :parameters}]
                           (let [page (if (:page query) (:page query) 1)
                                 per-page (if (:per_page query) (:per_page query) 10)
                                 results (parser/check-rules body)]
                             (if results (bad-request results)
                                 (let [query-map (parser/parse body)]
                                   (log/info "Query collections: " query-map query)
                                   (ok {:total (dc/count-coll query-map)
                                        :page page
                                        :per_page per-page
                                        :data (dc/query [(dc/find-coll query-map)
                                                         (dc/paginate {:page page :per-page per-page})])})))))}}]
   ["/count-collections"
    {:post  {:summary    "Get counts by group"
             :parameters {:query dc-spec/count-params-query :body any?}
             :responses  {200 {:body any?}}
             :handler    (fn [{{:keys [query body]} :parameters}]
                           (let [group (:group query)
                                 results (parser/check-rules body)]
                             (if results (bad-request results)
                                 (let [query-map (parser/parse body)]
                                   (log/info "Count collections: " query-map group query)
                                   (ok (dc/count-group-by query-map group))))))}}]])