(ns dataseq-core.events.update-schema
  (:require [clojure.core.async :as async]
            [clojure.tools.logging :as log]
            [dataseq-core.db.core :as db]
            [dataseq-core.events :as events]))

(def ^:const update-schema-topics
  "The `Set` of event topics which are subscribed to for use in update-schema tracking."
  #{:schema-update})

(def ^:private update-schema-channel
  "Channel for receiving event update-schema we want to subscribe to for update-schema events."
  (async/chan))

;;; ------------------------------------------------ Event Processing ------------------------------------------------

(defn- send-update-schema! [options]
  (db/update-schema! options))

(defn- process-update-schema-event!
  "Handle processing for a single event update-schema received on the update-schema-channel"
  [update-schema-event]
  ;; try/catch here to prevent individual topic processing exceptions from bubbling up.  better to handle them here.
  (try
    (when-let [{topic :topic object :item} update-schema-event]
      ;; TODO: only if the definition changed??
      (case (events/topic->model topic)
        "schema" (send-update-schema! {})))
    (catch Throwable e
      (log/warn (format "Failed to process update-schema event. %s" (:topic update-schema-event)) e))))

;;; --------------------------------------------------- Lifecycle ----------------------------------------------------

(defn events-init
  "Automatically called during startup; start event listener for update-schema events."
  []
  (events/start-event-listener! update-schema-topics update-schema-channel process-update-schema-event!))

