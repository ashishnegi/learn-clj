(ns joy-of-clojure.learntransducers)

(defn map-transduce
  [f]
  (fn [xf]
    (fn
      ([] (xf)) ;; delegate to inner transformer for initial value.
      ([one] (xf one))    ;; final reduction after my reduction be delegated
      ([one two] (xf one (f two))) ;; reducing over accumulaed and new value.
      )))
