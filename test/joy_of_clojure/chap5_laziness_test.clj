(ns joy-of-clojure.chap5-laziness-test
  (:require [joy-of-clojure.chap5-laziness :as laz]
            [clojure.test :as t]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]))

(def qsort-test
  (prop/for-all [v (gen/vector gen/int)]
                (= (laz/qsort v) (sort v))))

(tc/quick-check 100 qsort-test)
