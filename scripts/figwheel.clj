(require '[figwheel-sidecar.repl :as r]
         '[figwheel-sidecar.repl-api :as ra])

(ra/start-figwheel!
  {:figwheel-options {:server-port 3700}
   :build-ids ["devcards"]
   :all-builds
   [{:id "devcards"
     :figwheel {:devcards true}
     :source-paths ["src/main" "src/devcards" "src/test"]
     :compiler {:main 'plomber.devcards.core
                :asset-path "/devcards/out"
                :output-to "resources/public/devcards/main.js"
                :output-dir "resources/public/devcards/out"
                :parallel-build true
                :compiler-stats true}}]})

(ra/cljs-repl)
