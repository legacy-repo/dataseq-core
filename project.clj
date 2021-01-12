(defproject dataseq-core "0.2.1"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [[ch.qos.logback/logback-classic "1.2.3"]
                 [cheshire "5.10.0"]
                 [org.clojure/tools.namespace "1.0.0"]
                 [clojure.java-time "0.3.2"]
                 [org.clojure/core.async "0.4.500"
                  :exclusions [org.clojure/tools.reader]]
                 [colorize "0.1.1" :exclusions [org.clojure/clojure]]
                 [org.tcrawley/dynapath "1.0.0"]
                 [com.fasterxml.jackson.core/jackson-core "2.11.3"]
                 [com.fasterxml.jackson.core/jackson-databind "2.11.3"]
                 [com.google.guava/guava "27.0.1-jre"]
                 [com.novemberain/monger "3.1.0" :exclusions [com.google.guava/guava]]
                 [cprop "0.1.17"]
                 [expound "0.8.6"]
                 [funcool/struct "1.4.0"]
                 [luminus-transit "0.1.2"]
                 [luminus/ring-ttl-session "0.3.3"]
                 [markdown-clj "1.10.5"]
                 [metosin/jsonista "0.2.7"]
                 [metosin/muuntaja "0.6.7"]
                 [metosin/reitit "0.5.9"]
                 [metosin/ring-http-response "0.9.1"]
                 [mount "0.1.16"]
                 [nrepl "0.8.2"]
                 [org.clojure/clojure "1.10.1"]
                 [org.clojure/tools.cli "1.0.194"]
                 [org.clojure/tools.logging "1.1.0"]
                 [org.webjars.npm/bulma "0.9.1"]
                 [org.webjars.npm/material-icons "0.3.1"]
                 [org.webjars/webjars-locator "0.40"]
                 [ring-webjars "0.2.0"]
                 [ring/ring-core "1.8.2"]
                 [ring-cors "0.1.13"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-servlet "1.7.1"]
                 [selmer "1.12.31"]]

  :repositories [["central" "https://maven.aliyun.com/repository/central"]
                 ["jcenter" "https://maven.aliyun.com/repository/jcenter"]
                 ["clojars" "https://mirrors.tuna.tsinghua.edu.cn/clojars/"]
                 ["clojars-official" "https://clojars.org/repo/"]]

  :plugin-repositories [["central" "https://maven.aliyun.com/repository/central"]
                        ["jcenter" "https://maven.aliyun.com/repository/jcenter"]
                        ["clojars" "https://mirrors.tuna.tsinghua.edu.cn/clojars/"]]

  :min-lein-version "2.0.0"

  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :resource-paths ["resources"]
  :target-path "target/%s/"
  :main ^:skip-aot dataseq-core.core

  :plugins [[lein-uberwar "0.2.1"]]
  :uberwar
  {:handler dataseq-core.handler/app
   :init dataseq-core.handler/init
   :destroy dataseq-core.handler/destroy
   :name "dataseq-core.war"}


  :profiles
  {:uberjar {:omit-source false  ; You can't set to true, if you want to make the findnamespace valid (for tasks/events).
             :aot :all
             :uberjar-name "dataseq-core.jar"
             :source-paths ["env/prod/clj"]
             :resource-paths ["env/prod/resources"]
             :dependencies [[luminus-jetty "0.2.0"]]}

   :dev           [:project/dev :profiles/dev]
   :test          [:project/dev :project/test :profiles/test]

   :project/dev  {:jvm-opts ["-Dconf=dev-config.edn"]
                  :dependencies [[directory-naming/naming-java "0.8"]
                                 [luminus-jetty "0.2.0"]
                                 [pjstadig/humane-test-output "0.10.0"]
                                 [prone "2020-01-17"]
                                 [ring/ring-devel "1.8.2"]
                                 [ring/ring-mock "0.4.0"]]
                  :plugins      [[com.jakemccrary/lein-test-refresh "0.24.1"]
                                 [jonase/eastwood "0.3.5"]]

                  :source-paths ["env/dev/clj"]
                  :resource-paths ["env/dev/resources"]
                  :repl-options {:init-ns user
                                 :timeout 120000}
                  :injections [(require 'pjstadig.humane-test-output)
                               (pjstadig.humane-test-output/activate!)]}
   :project/test {:jvm-opts ["-Dconf=test-config.edn"]
                  :resource-paths ["env/test/resources"]}
   :profiles/dev {}
   :profiles/test {}})
