(ns dataseq-core.config
  (:require
    [cprop.core :refer [load-config]]
    [cprop.source :as source]
    [mount.core :refer [args defstate]]))

(defstate env
  :start
  (load-config
    :merge
    [(args)
     (source/from-system-props)
     (source/from-env)]))

(def ^Boolean is-test? "Are we running in `test` mode (i.e. via `lein test`)?"                    (= "test" (:dataseq-run-mode env)))

(defn get-workdir
  []
  (get-in env [:datarepo-workdir]))