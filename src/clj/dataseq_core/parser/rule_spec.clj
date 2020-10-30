(ns dataseq-core.parser.rule-spec
  (:require [clojure.spec.alpha :as s]
            [spec-tools.core :as st]))

;; ------------------------------------------ Rule ------------------------------------------
(s/def ::type
  (st/spec
   {:spec                #(#{"rule"} %)
    :type                :string
    :description         "The type of rule, only support rule and group."
    :swagger/default     "rule"
    :reason              "The type of rule, only support rule and group."}))

(s/def ::operator
  (st/spec
   {:spec                #(#{"=" ">" "<" ">=" "<=" "in" "not in" "!=" "regex"} %)
    :type                :string
    :description         "Operator, only support one of #{'=' '>' '<' '>=' '<=' 'in' 'not in' '!=' 'regex'}"
    :swagger/default     "="
    :reason              "Operator, only support one of #{'=' '>' '<' '>=' '<=' 'in' 'not in' '!=' 'regex'}"}))

(s/def ::variable
  (st/spec
   {:spec                string?
    :type                :string
    :description         "The variable name."
    :swagger/default     "age"
    :reason              "The variable name must be string."}))

(s/def ::value
  (st/spec
   {:spec                any?
    :type                :any
    :description         "Value"
    :swagger/default     100
    :reason              "Value"}))

(s/def ::query
  (s/keys :req-un [::variable ::operator ::value]))

(s/def ::rule
  (s/keys :req-un [::type ::query]))

(def rule ::rule)