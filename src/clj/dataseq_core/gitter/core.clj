(ns dataseq-core.gitter.core
  (:require [clj-jgit.porcelain :as c]
            [clj-jgit.querying :as q]
            [clj-jgit.internal :as i]
            [clojure.data.json :as json]
            [dataseq-core.util :as u]
            [dataseq-core.file-manager.local :as local]
            [dataseq-core.file-manager.fs :as fs])
  (:import [clojure.lang PersistentVector PersistentHashMap]
           [org.eclipse.jgit.treewalk TreeWalk]
           [org.eclipse.jgit.revwalk RevCommit RevTree]
           [org.eclipse.jgit.api Git]))

(defn exist-commit?
  [path commit-ish]
  (c/with-repo path (q/find-rev-commit repo rev-walk commit-ish)))

(defn init-repo!
  ^Git [path]
  (if-not (fs/exists? path)
    (do
      (fs/create-directories path)
      (c/git-init :dir path))
    (c/load-repo path)))

(defn list-commits
  [^Git repo]
  (map (fn [rev-commit]
         (select-keys (q/commit-info repo rev-commit)
                      [:author :branches :changed_files :email :id :merge :message :time]))
       (q/rev-list repo)))

(defn- clean-unwanted-file
  ^java.util.TreeSet
  [^Git repo]
  (c/git-clean repo :dirs? true :ignore? true :force? true))

(defn clean-unwanted-files
  [^Git repo]
  (loop [tree-set (clean-unwanted-file repo)
         results []]
    (if (.isEmpty tree-set)
      results
      (recur (clean-unwanted-file repo)
             (conj results (.first tree-set))))))

(defn- build-rev-commit
  [^Git repo ^String commit-ish]
  (q/find-rev-commit repo (i/new-rev-walk repo) commit-ish))

(defn- build-tree-walk
  [^Git repo ^String path ^RevTree tree]
  (TreeWalk/forPath (.getRepository repo) path tree))

(defn- new-tree-walk
  [^Git repo ^String path ^RevCommit rev-commit]
  (if path
    (let [dir-walk  (TreeWalk. (.getRepository repo))
          tree-walk (build-tree-walk repo path (.getTree rev-commit))]
      (if (some? tree-walk)
        (do
          (.addTree dir-walk (.getObjectId tree-walk 0))
          (.setRecursive tree-walk false)
          dir-walk)
        nil))
    (let [tree-walk (i/new-tree-walk repo rev-commit)]
      (.setRecursive tree-walk false)
      tree-walk)))

(defn- query-by-treewalk
  [^Git repo ^String commit-ish f & args]
  (let [rev-commit (build-rev-commit repo commit-ish)
        args       (concat [repo rev-commit] args)]
    (apply f args)))

(defn list-files
  [^Git repo ^String commit-ish ^String subpath]
  (let [tree-walk (new-tree-walk repo subpath (build-rev-commit repo commit-ish))]
    (if (nil? tree-walk)
      []
      (loop [results []]
        (if (not (.next tree-walk))
          results
          (recur (conj results (.getPathString tree-walk))))))))

(defn list-files-details
  "List the details for all files that located in the repo directory or the specified repo commit."
  [^Git repo ^String
   & {:keys [^String commit-ish ^String subpath ^PersistentVector files]
      :or {commit-ish nil subpath nil files []}}]
  (let [path (.getPath (.getWorkTree (.getRepository repo)))
        base-dir (if subpath (fs/join-paths path subpath) path)]
    (if (nil? commit-ish)
      (->> (fs/list-files base-dir)
           (filter (fn [file-path]
                     (if (empty? files)
                       true
                       (>= (.indexOf files
                                     (fs/join-paths subpath file-path))
                           0))))
           (map #(local/file-details (fs/join-paths base-dir %) subpath)))
      (->> (query-by-treewalk repo commit-ish list-files subpath)
           (map #(local/file-details (fs/join-paths base-dir %) subpath))))))

(defn clean?
  [^Git repo]
  (let [status (c/git-status repo)]
    (every? true? (map #(empty? (second %)) status))))

(defn status
  [^Git repo ^String path]
  (c/git-status repo :paths path))

(defn load-valid-files
  [template-name]
  nil)

(defn commit!
  [^Git repo ^String message ^PersistentVector file-list]
  (if (clean? repo)
    repo
    (do
      (c/git-add repo file-list)
      (c/git-commit repo message))))

(defn init-repo-meta!
  [^String path ^String name ^String description
   & {:keys [files config] :or {files [] config {}}}]
  (let [config-path (fs/join-paths path "manifest.json")]
    (spit (fs/as-file config-path)
          (json/write-str {:name name
                           :description description
                           :created (u/now)
                           :files files
                           :config config}))))

(defn update-repo-meta!
  [^String path
   & {:keys [^String description
             ^PersistentVector files
             ^PersistentHashMap config]}]
  (let [manifest-path (fs/join-paths path "manifest.json")
        manifest (json/read-str (slurp manifest-path))
        updated-manifest (into {} (filter (fn [[k v]] (some? v))
                                          {:description description
                                           :files files
                                           :config config}))]
    (spit manifest-path
          (json/write-str (merge manifest updated-manifest)))))

