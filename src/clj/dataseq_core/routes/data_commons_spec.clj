(ns dataseq-core.routes.data-commons-spec
  (:require [clojure.spec.alpha :as s]
            [spec-tools.core :as st]))

(s/def ::page
  (st/spec
   {:spec                nat-int?
    :type                :long
    :description         "Page, From 1."
    :swagger/default     1
    :reason              "The page parameter can't be none."}))

(s/def ::per_page
  (st/spec
   {:spec                nat-int?
    :type                :long
    :description         "Num of items per page."
    :swagger/default     10
    :reason              "The per-page parameter can't be none."}))

(def collection-params-query
  "A spec for the query parameters."
  (s/keys :req-un []
          :opt-un [::page ::per_page]))

(s/def ::group
  (st/spec
   {:spec                string?
    :type                :string
    :description         "group name"
    :reason              "The group parameter can't be none."}))

(def count-params-query
  "A spec for the query parameters."
  (s/keys :req-un []
          :opt-un [::group]))