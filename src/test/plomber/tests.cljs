(ns plomber.tests
  (:require [cljs.test :refer-macros [is are deftest run-tests]]
            [plomber.core :as plomber]))

(deftest test-generate-stats
  (is false)
  (is (= (plomber/generate-stats
           {:measurements {"Dashboard" {:will-mount 1761.62 :did-mount 1781.78 :mount-ts [20.16]}
                           "Post"      {:will-mount 1771.28 :did-mount 1777.94 :mount-ts [6.66]}}
            :sort-key :component-name
            :sort-asc? true})
         [{:component-name "Dashboard" :render-count 0 :mount-count 1
           :last-mount-ms 20.16 :avg-mount-ms 20.16 :max-mount-ms 20.16
           :min-mount-ms 20.16 :mount-std-dev 0}
          {:component-name "Post" :render-count 0 :mount-count 1
           :last-mount-ms 6.66 :avg-mount-ms 6.66 :max-mount-ms 6.66
           :min-mount-ms 6.66 :mount-std-dev 0}]))
  (is (= (plomber/generate-stats
           {:measurements {"Dashboard" {:will-mount 1761.62 :did-mount 1781.78 :mount-ts [20.16]}
                           "Post"      {:will-mount 1771.28 :did-mount 1777.94 :mount-ts [6.66]}}
            :sort-key :component-name
            :sort-asc? false})
         [{:component-name "Post" :render-count 0 :mount-count 1
           :last-mount-ms 6.66 :avg-mount-ms 6.66 :max-mount-ms 6.66
           :min-mount-ms 6.66 :mount-std-dev 0}
          {:component-name "Dashboard" :render-count 0 :mount-count 1
           :last-mount-ms 20.16 :avg-mount-ms 20.16 :max-mount-ms 20.16
           :min-mount-ms 20.16 :mount-std-dev 0}]))
  (is (= (plomber/generate-stats
           {:measurements {"Dashboard" {:will-mount 1761.62, :did-mount 1781.78, :mount-ts [20.16]}
                           "Post"      {:will-mount 1771.28, :did-mount 1777.94, :mount-ts [6.66]
                                        :will-update 645750.25
                                        :did-update 645753.43
                                        :render-ts [4.15 3.85 4.42 4.04 4.38 4.36 4.94 5.83 5.32 3.88 3.72 4.31 3.89 3.66 3.46 2.53 3.45 3.22 2.92 2.72 3.175]}}
            :sort-key :component-name
            :sort-asc? true})
        [{:component-name "Dashboard" :render-count 0 :mount-count 1
          :last-mount-ms 20.16 :avg-mount-ms 20.16 :max-mount-ms 20.16
          :min-mount-ms 20.16 :mount-std-dev 0}
         {:component-name "Post" :render-count 21 :mount-count 1
          :last-render-ms 3.175 :last-mount-ms 6.66 :avg-render-ms 3.9154761904761903
          :avg-mount-ms 6.66 :max-render-ms 5.83 :max-mount-ms 6.66
          :min-render-ms 2.53 :min-mount-ms 6.66 :render-std-dev 0.801406360216221 :mount-std-dev 0}])))

(deftest test-compute-label
  (is (= (plomber/compute-label "#" true true) "# ⇧"))
  (is (= (plomber/compute-label "#" true false) "# ⇩"))
  (is (= (plomber/compute-label "#" false true) "#"))
  (is (= (plomber/compute-label "#" false false) "#")))
