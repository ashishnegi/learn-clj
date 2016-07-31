(ns joy-of-clojure.learntransducers)

;; http://kukuruku.co/hub/funcprog/clojure-transducers-reducers-and-other-stuff

(defn map-transduce
  [f]
  (fn [xf]
    (fn
      ([] (xf)) ;; delegate to inner transformer for initial value.
      ([final-val] (xf final-val))    ;; final reduction after my reduction be delegated
      ([acc val] (xf acc (f val))) ;; reducing over accumulaed and new value.
      )))

;; (map-transduce inc)
;;=> (fn [xf]
;;     (fn
;;       ([] (xf))
;;       ([a] (xf a))
;;       ([acc v] (xf acc (f v)))))

;; ((map-transduce inc) -)
;;=> (fn
;;     ([] (-))
;;     ([a] (- a))
;;     ([acc v] (- acc (f v))))


;; (reduce (identity -) 0 [1 2 3 4])
;; => -10
;; ~~ (- (- (- (- 0 1) 2) 3) 4)
;; (transduce identity - 0 [1 2 3 4])
;; => 10
;; incorrect!
;; (transduce completing - 0 [1 2 3 4])
;; => -10

(defn filter-transduce
  [pred]
  (fn [xf]
    (fn
      ([] (xf))
      ([final-val] (xf final-val))
      ([acc val] (if (pred val)
                   (xf acc val)
                   acc)))))

;; joy-of-clojure.learntransducers> (time (r/fold + (r/folder v (take 100500))))
;; "Elapsed time: 20.558038 msecs"
;; 122851357021
;; joy-of-clojure.learntransducers> (time (r/fold + (eduction (take 100500) v)))
;; "Elapsed time: 18.431962 msecs"
;; 5050074750
;; joy-of-clojure.learntransducers> (time (reduce + 0 (take 100500 v)))
;; "Elapsed time: 14.029671 msecs"
;; 5050074750


;; stateful transducers
(def xf (comp (map inc) (take 10)))
(time (transduce xf + (range)))
;; => 55
