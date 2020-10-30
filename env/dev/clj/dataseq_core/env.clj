(ns dataseq-core.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [dataseq-core.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[dataseq-core started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[dataseq-core has shut down successfully]=-"))
   :middleware wrap-dev})
