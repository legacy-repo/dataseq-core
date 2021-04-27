(ns dataseq-core.events.update-repo
  (:require [clojure.core.async :as async]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [dataseq-core.file-manager.fs :as fs]
            [dataseq-core.config :refer [get-workdir]]
            [dataseq-core.events :as events])
  (:import [java.io FileNotFoundException]))

(def ^:const update-repo-topics
  "The `Set` of event topics which are subscribed to for use in update-repo tracking."
  #{:repo-update})

(def ^:private update-repo-channel
  "Channel for receiving event update-repo we want to subscribe to for update-repo events."
  (async/chan))

;;; ------------------------------------------------ Event Processing ------------------------------------------------

(defn- send-update-repo! [options]
  (log/info "Update Manifest for All Repos...")
  (let [workdir (get-workdir)
        repos (fs/list-files workdir :filter-fn fs/directory?)
        all-manifest (fs/join-paths workdir "manifest.json")]
    (io/make-parents all-manifest)
    (->> (map
          (fn [repo]
            (try
              (let [manifest (fs/join-paths workdir repo "manifest.json")
                    manifest-str (slurp manifest)]
                (json/read-json manifest-str true))
              (catch FileNotFoundException _
                (log/warn (str "Not found " (fs/join-paths repo "manifest.json")))
                nil)))
          repos)
         (filter some?)
         (json/write-str)
         (spit all-manifest))))

(defn- process-update-repo-event!
  "Handle processing for a single event update-repo received on the update-repo-channel"
  [update-repo-event]
  ;; try/catch here to prevent individual topic processing exceptions from bubbling up.  better to handle them here.
  (try
    (when-let [{topic :topic object :item} update-repo-event]
      ;; TODO: only if the definition changed??
      (case (events/topic->model topic)
        "repo" (send-update-repo! {})))
    (catch Throwable e
      (log/warn (format "Failed to process update-repo event. %s" (:topic update-repo-event)) e))))

;;; --------------------------------------------------- Lifecycle ----------------------------------------------------

(defn events-init
  "Automatically called during startup; start event listener for update-repo events."
  []
  (events/start-event-listener! update-repo-topics update-repo-channel process-update-repo-event!))

