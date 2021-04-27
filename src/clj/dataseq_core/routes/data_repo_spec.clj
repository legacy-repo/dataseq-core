(ns dataseq-core.routes.data-repo-spec
  (:require [clojure.spec.alpha :as s]
            [spec-tools.core :as st]))

(s/def ::name
  (st/spec
   {:spec                (s/and string? #(re-find #"^[a-zA-Z][A-Za-z0-9_\-]{1,61}[A-Za-z0-9]$" %))  ; 不超过 64 个字符
    :type                :string
    :description         "Repo Name"
    :reason              "The field name can't be none."}))

(s/def ::commit-ish
  (st/spec
   {:spec                (s/and string? #(re-find #"^[a-zA-Z0-9]{1,40}$" %))  ; 不超过 40 个字符
    :type                :string
    :description         "Commit ID"
    :reason              "The field name can't be none."}))

(s/def ::subpath
  (st/spec
   {:spec                (s/and string? #(re-find #"^[a-zA-Z0-9/\-_]+$" %))
    :type                :string
    :description         "Subpath which is related with working directory"
    :reason              "The field name can't be none."}))

(s/def ::message
  (st/spec
   {:spec                string?
    :type                :string
    :description         "The message for a commit."
    :reason              "The field's description can't be none."}))

(def repo-name
  (s/keys :req-un [::name]
          :opt-un []))

(def repo-subpath
  (s/keys :req-un []
          :opt-un [::subpath]))

(def repo-file-query
  (s/keys :req-un [::commit-ish]
          :opt-un [::subpath]))

(s/def ::description
  (st/spec
   {:spec                string?
    :type                :string
    :description         "The Description of a Field"
    :reason              "The field's description can't be none."}))

(s/def ::files
  (st/spec
   {:spec                vector?
    :type                :array
    :description         "Supported Files."
    :reason              "The values must be a vector."}))

(def repo-commit-body
  (s/keys :req-un [::message]
          :opt-un [::files]))

(s/def ::executor
  (st/spec
   {:spec                string?
    :type                :string
    :description         "The Executor for File Merging."
    :reason              "The values must be a string."}))

(s/def ::input1
  (st/spec
   {:spec                string?
    :type                :string
    :description         "Input file path."
    :reason              "The values must be a string."}))

(s/def ::input2
  (st/spec
   {:spec                string?
    :type                :string
    :description         "Input file path."
    :reason              "The values must be a string."}))

(s/def ::argument
  (st/spec
   {:spec                string?
    :type                :string
    :description         "Arguments for merging files with executor."
    :reason              "The values must be a string."}))

(s/def ::output
  (st/spec
   {:spec                string?
    :type                :string
    :description         "Output file path."
    :reason              "The values must be a string."}))

(s/def ::operator
  (s/keys :req-un [::input1 ::input2 ::argument ::output]
          :opt-un []))

(s/def ::operators
  (s/coll-of ::operator))

(s/def ::config
  (s/keys :req-un [::executor ::operators]
          :opt-un []))

(s/def ::data-repo-body
  (s/keys :req-un [::name ::description]
          :opt-un [::files ::config]))
