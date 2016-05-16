(ns plomber.run-tests
  (:require [cljs.test :refer-macros [run-tests]]
            [cljs.nodejs]
            [plomber.tests]))

(enable-console-print!)

(defn main []
  (run-tests 'plomber.tests))

(set! *main-cli-fn* main)
