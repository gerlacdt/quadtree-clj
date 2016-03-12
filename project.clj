(defproject quadtree "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.trace "0.7.9"]
                 [cheshire "5.5.0"]
                 [criterium "0.4.4"]]
  :main ^:skip-aot quadtree.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :repl {:plugins [[cider/cider-nrepl "0.11.0-SNAPSHOT"]]}
             :dev {:dependencies [[org.clojure/tools.nrepl "0.2.12"]]}
             :benchmark {:test-paths ^:replace ["benchmarks"]
                         :jvm-opts ^:replace ["-Xms1g" "-Xmx1g" "-server"]}}
  :aliases {"benchmark" ["with-profile" "dev,benchmark" "test"]})
