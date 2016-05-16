(defproject plomber "0.1.0-SNAPSHOT"
  :description "Component instrumentation for Om Next"
  :url "http://github.com/anmonteiro/plomber"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0" :scope "provided"]
                 [org.clojure/clojurescript "1.8.51" :scope "provided"]
                 [figwheel-sidecar "0.5.3-1" :scope "test"]
                 [devcards-om-next "0.1.1" :scope "test"]]

  :profiles {:dev {:dependencies [[org.omcljs/om "1.0.0-alpha35-SNAPSHOT" :scope "provided"]]}
             :test {:dependencies [[org.omcljs/om "1.0.0-alpha35"]]
                    :plugins [[lein-doo "0.1.6"]
                              [lein-cljsbuild "1.1.3"]]
                    :cljsbuild {:builds [{:id           "test"
                                          :source-paths ["src/main" "src/test"]
                                          :compiler     {:output-to "target/js/client_test.js"
                                                         :output-dir "target/js/out"
                                                         :main          plomber.runner
                                                         :target :nodejs
                                                         :optimizations :none}}]}}}

  :source-paths ["src/main" "src/devcards" "src/test"]
  :clean-targets ^{:protect false} [:target-path
                                    "resources/public/devcards/out"
                                    "resources/public/devcards/main.js"]
  :target-path "target")
