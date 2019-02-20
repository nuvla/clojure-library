(def +version+ "0.0.1-SNAPSHOT")

(defproject sixsq.nuvla/clojure-library "0.0.1-SNAPSHOT"

  :description "nuvla clojure library"

  :url "https://github.com/nuvla/clojure-library"

  :license {:name         "Apache 2.0"
            :url          "http://www.apache.org/licenses/LICENSE-2.0.txt"
            :distribution :repo}

  :plugins [[lein-parent "0.3.5"]
            [lein-doo "0.1.11"]]

  :parent-project {:coords  [sixsq.nuvla/parent "6.1.3"]
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

  :codox {:name         "sixsq.nuvla/clojure-library"
          :version      ~+version+
          :source-paths #{"src/clj" "src/cljc"}
          :source-uri   "https://github.com/nuvla/clojure-library/blob/master/jar/{filepath}#L{line}"
          :language     :clojure
          :metadata     {:doc/format :markdown}}

  :doo {:verbose true
        :debug   true}

  :dependencies
  [[log4j]
   [com.cemerick/url]
   [org.clojure/data.json]
   [org.clojure/core.async]
   [io.nervous/kvlt]
   ]

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
                       ["with-profiles" "test" ["doo" "nashorn" "test" "once"]]]
            "docs"    ["codox"]})
