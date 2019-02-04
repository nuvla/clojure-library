(def +version+ "3.69-SNAPSHOT")

(defproject com.sixsq.slipstream/SlipStreamClojureAPI-cimi "3.69-SNAPSHOT"

  :description "Clojure CIMI API"

  :url "https://github.com/slipstream/SlipStreamClojureAPI"

  :license {:name         "Apache 2.0"
            :url          "http://www.apache.org/licenses/LICENSE-2.0.txt"
            :distribution :repo}

  :plugins [[lein-parent "0.3.2"]
            [lein-doo "0.1.8"]]

  :parent-project {:coords  [sixsq/slipstream-parent "5.3.19"]
                   :inherit [:plugins
                             :min-lein-version
                             :managed-dependencies
                             :repositories
                             :deploy-repositories]}

  :pom-location "target/"

  :source-paths ["src/clj" "src/cljc"]

  :clean-targets ^{:protect false} ["target" "out"]

  :aot [sixsq.slipstream.client.api.cimi
        sixsq.slipstream.client.api.authn]

  :codox {:name         "com.sixsq.slipstream/SlipStreamClojureAPI-cimi"
          :version      ~+version+
          :source-paths #{"src/clj" "src/cljc"}
          :source-uri   "https://github.com/slipstream/SlipStreamClojureAPI/blob/master/jar/{filepath}#L{line}"
          :language     :clojure
          :metadata     {:doc/format :markdown}}

  :doo {:verbose true
        :debug   true}

  :dependencies
  [[org.clojure/tools.logging]                              ;; run utils
   [log4j]                                                  ;; run utils
   [com.cemerick/url]
   [clojure-ini]                                            ;; run utils
   [org.clojure/data.json]
   [org.clojure/data.xml]                                   ;; run utils
   [org.clojure/core.async]
   [io.nervous/kvlt]]

  :cljsbuild {:builds [{:id           "test"
                        :source-paths ["test/cljc" "test/cljs"]
                        :compiler     {:main          'sixsq.slipstream.client.runner
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
