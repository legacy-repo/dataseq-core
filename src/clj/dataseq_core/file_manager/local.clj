(ns dataseq-core.file-manager.local
  (:require [clojure.string :as clj-str]
            [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [digest]
            [dataseq-core.file-manager.fs :as fm-fs]))

(def ^:private root-dir (atom ""))

(defn setup-root-dir
  [dir]
  (reset! root-dir dir))

(defn created-time
  [path]
  (.creation (io/file path)))

(defn get-files
  [body-path]
  (let [path (clj-str/replace body-path #"(\.\.\/)" "")
        dir  (str @root-dir path)]
    (file-seq (io/file dir))))

(defn check-for-duplicates
  [directory name is-file?]
  (let [files   (file-seq (io/file directory))
        path    (str directory "/" name)
        matched (filter #(= % name) files)]
    (cond
      (nil? matched) false
      (and (not is-file?)
           (fs/directory? path)) true
      (and is-file? (fs/file? path)) true
      :else false)))

(defn get-ext
  [path]
  (last (fs/split-ext path)))

(defn file-details
  [filepath filter-path]
  {:name     (fs/base-name filepath)
   :size     (fs/size filepath)
   :isFile   (fs/file? filepath)
   :modified (fs/mod-time filepath)
   :created  (str (fm-fs/creation-time filepath))
   :type     (get-ext filepath)
   :md5sum   nil
   :location (if (nil? filter-path) "." filter-path)})

(defn calc-dir-size [f]
  (if (.isDirectory f)
    (apply + (pmap calc-dir-size (.listFiles f)))
    (.length f)))

(defn get-folder-size
  [directory]
  (calc-dir-size directory))

(defn get-size
  [size]
  (cond
    (< size 1024) (str size " B")
    (< size (* 1024 1024)) (str (/ size (* 1.0 1024)) " KB")
    (< size (* 1024 1024 1024)) (str (/ size (* 1.0 1024 1024)) " MB")
    :else (str (/ size (* 1.0 1024 1024 1024)) " GB")))

(defn copy-folder
  [source dest]
  (fs/copy-dir source dest))

(def random (java.util.Random.))

(def chars-map
  (map char (concat (range 48 58) (range 66 92) (range 97 123))))

(defn random-char
  "Generates 1 random character"
  []
  (nth chars-map (.nextInt random (count chars-map))))

(defn random-string
  "Generates random string of length characters"
  [length]
  (apply str (take length (repeatedly random-char))))

(defn update-copy-name
  [path]
  (let [extension (get-ext path)
        base-name (fs/base-name path true)]
    (if extension
      (str base-name "-" (random-string 5) "." extension)
      (str name "-" (random-string 5)))))

(defn get-relative-path
  [root-dir full-path]
  (let [root-dir      (clj-str/replace root-dir #".*\/$" "")
        relative-path (clj-str/replace full-path (re-pattern (str root-dir "/")))]
    (if (= relative-path full-path)
      ""
      relative-path)))

