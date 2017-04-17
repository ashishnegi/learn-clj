(ns learn-clj.chap5-laziness-test
  (:require [learn-clj.chap5-laziness :as laz]
            [clojure.test :as t]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]))

(def qsort-test
  (prop/for-all [v (gen/vector gen/int)]
                (= (laz/qsort v) (sort v))))

(tc/quick-check 100 qsort-test)
