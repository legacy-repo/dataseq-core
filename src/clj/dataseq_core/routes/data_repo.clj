(ns dataseq-core.routes.data-repo
  (:require [clojure.tools.logging :as log]
            [clojure.data.json :as json]
            [ring.util.http-response :refer [ok bad-request]]
            [dataseq-core.events :as events]
            [dataseq-core.config :refer [get-workdir]]
            [dataseq-core.routes.data-repo-spec :as dr-spec]
            [dataseq-core.file-manager.fs :as fs]
            [dataseq-core.gitter.core :as gitter]))

(def data-repo
  [""
   {:swagger {:tags ["Data Repo"]}}

   ["/repo"
    {:get {:summary "Get all valid repos."
           :parameters {}
           :responses {200 {:body any?}}
           :handler (fn [{{{:keys [_]} :query} :parameters}]
                      (ok (json/read-str (slurp (fs/join-paths (get-workdir) "manifest.json"))
                                         :key-fn keyword)))}

     :post  {:summary    "Create a data repo."
             :parameters {:body ::dr-spec/data-repo-body}
             :responses  {201 {:body {:id string?}}
                          400 {:body {:message string?}}}
             :handler    (fn [{{:keys [body]} :parameters}]
                           (log/info "Create a data repo: " body)
                           (try
                             (let [repo-path (fs/join-paths (get-workdir) (:name body))]
                               (if (fs/exists? repo-path)
                                 (bad-request {:message "Data repo exists."})
                                 (do
                                   (gitter/init-repo! repo-path)
                                   (let [repo (gitter/init-repo-meta! repo-path
                                                                      (:name body)
                                                                      (:description body)
                                                                      :files (:files body)
                                                                      :config (:config body))]
                                     (events/publish-event! :repo-update {})
                                     (ok {:id (str (:_id repo))})))))
                             (catch Throwable e
                               (log/error e "Unexpected error when initializing repo"))))}

     :put  {:summary    "Update a data repo."
            :parameters {:body ::dr-spec/data-repo-body}
            :responses  {201 {:body {:id string?}}
                         400 {:body {:message string?}}}
            :handler    (fn [{{:keys [body]} :parameters}]
                          (log/info "Update a data repo: " body)
                          (try
                            (let [repo-path (fs/join-paths (get-workdir) (:name body))]
                              (if (not (fs/exists? repo-path))
                                (bad-request {:message "Data repo doesn't exist."})
                                (do
                                  (gitter/init-repo! repo-path)
                                  (let [repo (apply gitter/update-repo-meta! repo-path
                                                    (-> (select-keys body [:description :files :config]) vec flatten))]
                                    (events/publish-event! :repo-update {})
                                    (ok {:id (str (:_id repo))})))))
                            (catch Throwable e
                              (log/error e "Unexpected error when initializing repo"))))}}]

   ["/repo/:name/commits"
    {:get  {:summary    "List all commits in a data repo."
            :parameters {:path dr-spec/repo-name}
            :responses  {200 {:body any?}
                         400 {:body {:message string?}}}
            :handler    (fn [{{{:keys [name]} :path} :parameters}]
                          (log/info "List all commits in the repo:" name)
                          (let [repo-path (fs/join-paths (get-workdir) name)]
                            (if (fs/exists? repo-path)
                              (let [repo (gitter/init-repo! repo-path)]
                                (ok (gitter/list-commits repo)))
                              (bad-request {:message "Repo doesn't exist."}))))}

     :post  {:summary    "Create a new commit in the data repo."
             :parameters {:path dr-spec/repo-name
                          :body dr-spec/repo-commit-body}
             :responses  {200 {:body any?}
                          400 {:body {:message string?}}}
             :handler    (fn [{{{:keys [name]} :path
                                {:keys [message files]} :body} :parameters}]
                           (log/info "Create a new commit in the repo:" name)
                           (let [repo-path (fs/join-paths (get-workdir) name)
                                 manifest (json/read (slurp (fs/join-paths repo-path "manifest.json")))]
                             (if (fs/exists? repo-path)
                               (let [repo (gitter/init-repo! repo-path)]
                                 (ok (gitter/commit! repo message (into files (:files manifest)))))
                               (bad-request {:message "Repo doesn't exist."}))))}}]

   ["/repo/:name/status"
    {:get  {:summary    "Get the status of a data repo."
            :parameters {:path dr-spec/repo-name
                         :query dr-spec/repo-subpath}
            :responses  {200 {:body any?}
                         400 {:body {:message string?}}}
            :handler    (fn [{{{:keys [name]} :path
                               {:keys [subpath]} :query} :parameters}]
                          (log/info "Get the status of the data repo:" name)
                          (let [repo-path (fs/join-paths (get-workdir) name)]
                            (if (fs/exists? repo-path)
                              (let [repo (gitter/init-repo! repo-path)]
                                (ok (gitter/status repo subpath)))
                              (bad-request {:message "Repo doesn't exist."}))))}}]

   ["/repo/:name/files"
    {:get  {:summary    "List all files in a data repo."
            :parameters {:path dr-spec/repo-name
                         :query dr-spec/repo-file-query}
            :responses  {200 {:body any?}
                         400 {:body {:message string?}}}
            :handler    (fn [{{{:keys [name]} :path
                               {:keys [commit-ish subpath]} :query} :parameters}]
                          (log/info "List all files in the repo:" name commit-ish)
                          (let [repo-path (fs/join-paths (get-workdir) name)
                                manifest (json/read-str (slurp (fs/join-paths repo-path "manifest.json")))]
                            (if (and (fs/exists? repo-path) (some? (gitter/exist-commit? repo-path commit-ish)))
                              (let [repo (gitter/init-repo! repo-path)]
                                (ok (gitter/list-files-details repo
                                                               :commit-ish commit-ish
                                                               :subpath subpath
                                                               :files (:files manifest))))
                              (bad-request {:message "Repo/Commit-ish doesn't exist."}))))}}]])
