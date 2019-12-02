(def parent-version "6.7.1")

(defproject sixsq.nuvla/api "2.0.7-SNAPSHOT"

  :description "nuvla clojure library"

  :url "https://github.com/nuvla/clojure-library"

  :license {:name         "Apache 2.0"
            :url          "http://www.apache.org/licenses/LICENSE-2.0.txt"
            :distribution :repo}

  :plugins [[lein-parent "0.3.5"]
            [lein-doo "0.1.11"]]

  :parent-project {:coords  [sixsq.nuvla/parent ~parent-version]
                   :inherit [:plugins
                             :min-lein-version
                             :managed-dependencies
                             :repositories
                             :deploy-repositories]}

  :pom-location "target/"

  :source-paths ["src/clj" "src/cljc"]

  :clean-targets ^{:protect false} ["target" "out"]

  :aot [sixsq.nuvla.client.api
        sixsq.nuvla.client.authn]

  :doo {:verbose true
        :debug   true}

  :dependencies
  [[log4j]
   [com.cemerick/url]
   [org.clojure/data.json]
   [org.clojure/core.async]
   [io.nervous/kvlt]]

  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "--no-sign"]
                  ["deploy" "clojars"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]]

  :vcs :git

  :cljsbuild {:builds [{:id           "test"
                        :source-paths ["test/cljc" "test/cljs"]
                        :compiler     {:main          sixsq.nuvla.client.runner
                                       :output-to     "target/clienttest.js"
                                       :output-dir    "target"
                                       :optimizations :whitespace}}]}

  :profiles {:provided {:dependencies [[org.clojure/clojure]
                                       [org.clojure/clojurescript]]}
             :test     {:aot            :all
                        :source-paths   ["test/clj" "test/cljc"]
                        :resource-paths ["dev-resources"]}}

  :aliases {"test"    ["do"
                       ["test"]
                       ["with-profiles" "test" ["doo" "nashorn" "test" "once"]]]})
