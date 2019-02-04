(def +version+ "3.69-SNAPSHOT")

(defproject sixsq.nuvla/clojure-api-cimi "3.69-SNAPSHOT"

  :description "Clojure CIMI API"

  :url "https://github.com/nuvla/clojure-api"

  :license {:name         "Apache 2.0"
            :url          "http://www.apache.org/licenses/LICENSE-2.0.txt"
            :distribution :repo}

  :plugins [[lein-parent "0.3.2"]
            [lein-doo "0.1.8"]]

  :parent-project {:coords  [sixsq.nuvla/parent "6.0.0"]
                   :inherit [:plugins
                             :min-lein-version
                             :managed-dependencies
                             :repositories
                             :deploy-repositories]}

  :pom-location "target/"

  :source-paths ["src/clj" "src/cljc"]

  :clean-targets ^{:protect false} ["target" "out"]

  :aot [sixsq.nuvla.client.api.cimi
        sixsq.nuvla.client.api.authn]

  :codox {:name         "sixsq.nuvla/clojure-api"
          :version      ~+version+
          :source-paths #{"src/clj" "src/cljc"}
          :source-uri   "https://github.com/nuvla/clojure-api/blob/master/jar/{filepath}#L{line}"
          :language     :clojure
          :metadata     {:doc/format :markdown}}

  :doo {:verbose true
        :debug   true}

  :dependencies
  [[log4j]
   [com.cemerick/url]
   [org.clojure/data.json]
   [org.clojure/core.async]
   [io.nervous/kvlt]]

  :cljsbuild {:builds [{:id           "test"
                        :source-paths ["test/cljc" "test/cljs"]
                        :compiler     {:main          'sixsq.nuvla.client.runner
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
            "docs"    ["codox"]
            "publish" ["shell" "../publish-docs.sh"]})
