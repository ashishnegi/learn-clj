(ns joy-of-clojure.learntransducers)

(defn map-transduce
  [f]
  (fn [xf]
    (fn
      ([] (xf)) ;; delegate to inner transformer for initial value.
      ([final-val] (xf final-val))    ;; final reduction after my reduction be delegated
      ([acc val] (xf acc (f val))) ;; reducing over accumulaed and new value.
      )))

(defn filter-transduce
  [pred]
  (fn [xf]
    (fn
      ([] (xf))
      ([final-val] (xf final-val))
      ([acc val] (if (pred val)
                   (xf acc val)
                   acc)))))
