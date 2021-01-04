(ns dataseq-core.util
  "Common utility functions useful throughout the codebase."
  (:require [clojure.tools.namespace.find :as ns-find]
            [colorize.core :as colorize]
            [clojure.java.classpath :as classpath]
            [dataseq-core.plugins.classloader :as classloader]))

(defn- namespace-symbs* []
  (for [ns-symb (distinct
                 (ns-find/find-namespaces (concat (classpath/system-classpath)
                                                  (classpath/classpath (classloader/the-classloader)))))
        :when   (and (.startsWith (name ns-symb) "dataseq-core.")
                     (not (.contains (name ns-symb) "test")))]
    ns-symb))

(def dataseq-core-namespace-symbols
  "Delay to a vector of symbols of all dataseq-core namespaces, excluding test namespaces.
    This is intended for use by various routines that load related namespaces, such as task and events initialization.
    Using `ns-find/find-namespaces` is fairly slow, and can take as much as half a second to iterate over the thousand
    or so namespaces that are part of the dataseq-core project; use this instead for a massive performance increase."
  ;; We want to give JARs in the ./plugins directory a chance to load. At one point we have this as a future so it
  ;; start looking for things in the background while other stuff is happening but that meant plugins couldn't
  ;; introduce new dataseq-core namespaces such as drivers.
  (delay (vec (namespace-symbs*))))

(def ^:private ^{:arglists '([color-symb x])} colorize
  "Colorize string `x` with the function matching `color` symbol or keyword."
  (fn [color x]
    (colorize/color (keyword color) x)))

(defn format-color
  "Like `format`, but colorizes the output. `color` should be a symbol or keyword like `green`, `red`, `yellow`, `blue`,
  `cyan`, `magenta`, etc. See the entire list of avaliable
  colors [here](https://github.com/ibdknox/colorize/blob/master/src/colorize/core.clj).

      (format-color :red \"Fatal error: %s\" error-message)"
  {:style/indent 2}
  (^String [color x]
   {:pre [((some-fn symbol? keyword?) color)]}
   (colorize color (str x)))

  (^String [color format-string & args]
   (colorize color (apply format (str format-string) args))))
