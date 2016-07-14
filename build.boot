(def +version+ "0.1.0-SNAPSHOT")

(set-env!
 :source-paths    #{"src/main"}
 :resource-paths  #{"resources"}
 :dependencies '[[org.clojure/clojurescript   "1.8.51"         :scope "provided"]
                 [org.omcljs/om               "1.0.0-alpha40"  :scope "provided"]
                 [com.cognitect/transit-clj   "0.8.285"        :scope "test"]
                 [devcards                    "0.2.1-7"        :scope "test"]
                 [devcards-om-next            "0.1.1"          :scope "test"]
                 [doo                         "0.1.7"          :scope "test"]
                 [com.cemerick/piggieback     "0.2.1"          :scope "test"]
                 [pandeiro/boot-http          "0.7.3"          :scope "test"]
                 [adzerk/boot-cljs            "1.7.228-1"      :scope "test"]
                 [adzerk/boot-cljs-repl       "0.3.2"          :scope "test"]
                 [crisptrutski/boot-cljs-test "0.2.2-SNAPSHOT" :scope "test"]
                 [adzerk/boot-reload          "0.4.11"         :scope "test"]
                 [adzerk/bootlaces            "0.1.13"         :scope "test"]
                 [org.clojure/tools.nrepl     "0.2.12"         :scope "test"]
                 [org.clojure/tools.namespace "0.3.0-alpha3"   :scope "test"]
                 [weasel                      "0.7.0"          :scope "test"]])

(require
 '[adzerk.boot-cljs      :refer [cljs]]
 '[adzerk.boot-cljs-repl :as cr :refer [cljs-repl-env start-repl]]
 '[adzerk.boot-reload    :refer [reload]]
 '[adzerk.bootlaces      :refer [bootlaces! push-release]]
 '[clojure.tools.namespace.repl :as repl]
 '[crisptrutski.boot-cljs-test :refer [prep-cljs-tests]]
 '[pandeiro.boot-http :refer [serve]]
 '[clojure.java.io :as io]
 '[doo.core :as doo])

(bootlaces! +version+ :dont-modify-paths? true)

(task-options!
  pom {:project 'plomber
       :version +version+
       :description "Component instrumentation for Om Next"
       :url "http://github.com/anmonteiro/plomber"
       :scm {:url "https://github.com/anmonteiro/plomber"}
       :license {"name" "Eclipse Public License"
                 "url"  "http://www.eclipse.org/legal/epl-v10.html"}})

(deftask deps [])

(deftask devcards []
  (set-env! :source-paths #(conj % "src/devcards"))
  (comp
    (serve)
    (watch)
    (cljs-repl-env)
    (reload)
    (speak)
    (cljs :source-map true
          :compiler-options {:devcards true
                             :parallel-build true}
          :ids #{"js/devcards"})
    (target :dir #{"target"})))

(deftask testing []
  (set-env! :source-paths #(conj % "src/test"))
  identity)

(deftask test-cljs
  [e exit?     bool  "Enable flag."]
  (let [exit? (cond-> exit?
                (nil? exit?) not)
        suite-ns 'plomber.run-tests
        cljs-opts (merge {:main suite-ns
                          :optimizations :none
                          :target :nodejs
                          :parallel-build true})]
    (comp
      (testing)
      (prep-cljs-tests
        :namespaces #{'plomber.tests}
        :suite-ns suite-ns
        :out-file "output.js")
      (cljs :ids #{"output"}
        :compiler-options cljs-opts)
      (target))))
