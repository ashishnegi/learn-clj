(ns joy-of-clojure.learntransducers-test
  (:require [joy-of-clojure.learntransducers :as trans]
            [clojure.test :as t]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]))

(def map-transduce-inc-test
  (prop/for-all
    [v (gen/vector gen/int)]
    (= (transduce (trans/map-transduce inc) + v)
       (transduce (map inc) + v))))

(tc/quick-check 200 map-transduce-test)

(def map-transduce-rand-transducers-test
  (prop/for-all
    [v (gen/vector gen/int)]
    (let [fns (gen/sample (gen/elements [inc inc dec #(+ 3 %)]))
          core-transducer (apply comp (map #(map %) fns))
          my-transducer (apply comp (map #(map %) fns))]
      (= (transduce core-transducer + v)
         (transduce my-transducer + v)))))

(tc/quick-check 100 map-transduce-rand-transducers-test)
