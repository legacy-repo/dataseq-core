(ns dataseq-core.parser.group-rule-spec
  (:require [clojure.spec.alpha :as s]
            [spec-tools.core :as st]
            [dataseq-core.parser.rule-spec :as rs]))

;; ------------------------------------------ Group Rule ------------------------------------------
(s/def ::type
  (st/spec
   {:spec                #(#{"group"} %)
    :type                :string
    :description         "The type of rule, only support group."
    :swagger/default     "group"
    :reason              "The type of rule, only support group."}))

(s/def ::operator
  (st/spec
   {:spec                #(#{"and" "or"} %)
    :type                :string
    :description         "Operator, only support one of #{'and' 'or'}"
    :swagger/default     "and"
    :reason              "Operator, only support one of #{'and' 'or'}"}))

(s/def ::vector-entry
  (s/or :rule rs/rule :group-rule ::group-rule))

(s/def ::children
  (s/coll-of ::vector-entry :kind vector))

(s/def ::group-rule
  (s/keys :req-un [::type ::operator ::children]))


;; ------------------------------------------ Reference Rule --------------------------------------
(s/def ::any-rule
  (s/or :rule rs/rule :group-rule ::group-rule :empty empty?))

(def group-rule ::group-rule)

(def any-rule ::any-rule)

(def children ::children)