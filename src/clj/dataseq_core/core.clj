(ns dataseq-core.core
  (:require
   [dataseq-core.handler :as handler]
   [dataseq-core.nrepl :as nrepl]
   [luminus.http-server :as http]
   [dataseq-core.events :as events]
   [dataseq-core.config :refer [env]]
   [dataseq-core.db.core :as dc]
   [clojure.tools.cli :refer [parse-opts]]
   [clojure.tools.logging :as log]
   [mount.core :as mount])
  (:gen-class))

;; log uncaught exceptions in threads
(Thread/setDefaultUncaughtExceptionHandler
 (reify Thread$UncaughtExceptionHandler
   (uncaughtException [_ thread ex]
     (log/error {:what :uncaught-exception
                 :exception ex
                 :where (str "Uncaught exception on" (.getName thread))}))))

(def cli-options
  [["-p" "--port PORT" "Port number"
    :parse-fn #(Integer/parseInt %)]])

(mount/defstate data-commons
  :start
  (dc/setup-connection!)
  :stop
  (dc/stop-connection!))

(mount/defstate event
  :start
  (events/initialize-events!)
  :stop
  (events/stop-events!))

(mount/defstate ^{:on-reload :noop} http-server
  :start
  (http/start
   (-> env
       (assoc  :handler (handler/app))
       (update :port #(or (-> env :options :port) %))))
  :stop
  (http/stop http-server))

(mount/defstate ^{:on-reload :noop} repl-server
  :start
  (when (env :nrepl-port)
    (nrepl/start {:bind (env :nrepl-bind)
                  :port (env :nrepl-port)}))
  :stop
  (when repl-server
    (nrepl/stop repl-server)))


(defn init-jndi []
  (System/setProperty "java.naming.factory.initial"
                      "org.apache.naming.java.javaURLContextFactory")
  (System/setProperty "java.naming.factory.url.pkgs"
                      "org.apache.naming"))

(defn start-app [args]
  (init-jndi)
  (doseq [component (-> args
                        (parse-opts cli-options)
                        mount/start-with-args
                        :started)]
    (log/info component "started"))
  ; Update data schema for all collections
  (events/publish-event! :schema-update {})
  ; Update manifest for all datarepo
  (events/publish-event! :repo-update {})   
  (.addShutdownHook (Runtime/getRuntime) (Thread. handler/destroy)))

(defn -main
  "Launch dataseq-core in standalone mode."
  [& args]
  (log/info "Starting dataseq-core in STANDALONE mode")
  ; Load configuration from system-props & env
  (mount/start #'dataseq-core.config/env)
  (cond
    ; When the DATABASE_URL variable has been set as "", an exception will be raised.
    ; #error: URI connection string cannot be empty!
    (or (nil? (:mongo-uri env)) (= "" (:mongo-uri env)))
    (do
      (log/error "Database configuration not found, :mongo-uri environment variable must be set before running")
      (System/exit 1))
    :else
    (start-app args)))   ; with no command line args just start Datains normally
