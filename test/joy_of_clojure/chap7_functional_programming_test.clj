(ns learn-clj.chap7-functional-programming-test
  (:require [learn-clj.chap7-functional-programming :as fp]
            [clojure.test :as t]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]))

(def gcd-test
  (prop/for-all [a gen/int
                 b gen/int]
                (= (fp/gcd-recursive a b) (fp/gcd-recur a b))))

(tc/quick-check 1000 gcd-test)
