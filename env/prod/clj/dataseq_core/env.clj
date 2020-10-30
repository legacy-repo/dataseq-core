(ns dataseq-core.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[dataseq-core started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[dataseq-core has shut down successfully]=-"))
   :middleware identity})
