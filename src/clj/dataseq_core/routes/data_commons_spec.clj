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

(s/def ::coll-name
  (st/spec
   {:spec                (s/and string? #(re-find #"^[a-z][a-z0-9_]{1,61}[a-z0-9]$" %))  ; 不超过 64 个字符
    :type                :string
    :description         "Collection Name"
    :reason              "The collection name can't be none."}))

(def collection-name
  (s/keys :req-un [::coll-name]
          :opt-un []))

(s/def ::name
  (st/spec
   {:spec                (s/and string? #(re-find #"^[a-zA-Z][A-Za-z0-9_ ]{1,61}[A-Za-z0-9]$" %))  ; 不超过 64 个字符
    :type                :string
    :description         "Field Name"
    :reason              "The field name can't be none."}))

(s/def ::short
  (st/spec
   {:spec                (s/and string? #(re-find #"^[a-zA-Z][A-Za-z0-9_ ]{1,29}[A-Za-z0-9]$" %))  ; 不超过 64 个字符
    :type                :string
    :description         "Short Field Name"
    :reason              "The short field name can't be none."}))

(s/def ::key
  (st/spec
   {:spec                (s/and string? #(re-find #"^[a-z][a-z0-9_.]{1,61}[a-z0-9]$" %))  ; 不超过 64 个字符
    :type                :string
    :description         "Field Key"
    :reason              "The field key can't be none."}))

(s/def ::description
  (st/spec
   {:spec                string?
    :type                :string
    :description         "The Description of a Field"
    :reason              "The field's description can't be none."}))

(s/def ::type
  (st/spec
   {:spec                 #(#{"category" "number" "precision" "bool"} %)
    :description         "The Data Type of a Field"
    :reason              "The field's data type can't be none."}))

(s/def ::collection
  (st/spec
   {:spec                (s/and string? #(re-find #"^[a-z][a-z0-9_]{1,61}[a-z0-9]$" %))  ; 不超过 64 个字符
    :type                :string
    :description         "Collection Name"
    :reason              "The collection name can't be none."}))

(s/def ::priority
  (st/spec
   {:spec                number?
    :type                :number
    :description         "The Priority Level of the Dispalying."
    :reason              "The priority level need to a number."}))

(s/def ::values
  (st/spec
   {:spec                vector?
    :description         "Supported options."
    :reason              "The values must be a vector."}))

(def field
  (s/keys :req-un [::name ::short ::description ::key ::type ::collection ::priority]
          :opt-un [::values]))

(def fields
  (s/coll-of field))