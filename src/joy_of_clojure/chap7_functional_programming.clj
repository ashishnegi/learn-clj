(ns joy-of-clojure.chap7-functional-programming)

(defn gcd-recursive [a b]
  (if (= a 0)
    b
    (gcd-recursive (mod b a) a)))

(defn gcd-recur [a b]
  (if (= a 0)
    b
    (recur (mod b a) a)))

;; Trampoline example
;; ff-open -> ff-closed or Done
;; ff-closed -> ff-open (open event) / sf-closed (up event)
;; |||ly for second floor
;; :open, :close, :up, :down, :done

;; In the letfn : functions can refer to each other without being forward declared.
;; Also, they are not recursive but optimized non-stack calls.
;; Also all of the functions should return functions.
;; If they return value, trampoline stops at that point.
(defn elevator [events]
  (letfn [(ff-open [[e & es]]
            #(case e
               :close (ff-close es)
               :done true
               false))
          (ff-close [[e & es]]
            #(case e
               :open (ff-open es)
               :up (sf-close es)
               false))
          (sf-open [[e & es]]
            #(case e
               :close (sf-close es)
               :done true
               false))
          (sf-close [[e & es]]
            #(case e
               :down (ff-close es)
               :open (sf-open es)
               false))]
    (trampoline ff-open events)))

(elevator [:close])
(elevator [:close :up :open :close :down :open :done])
;;=> true
(elevator [:close :up :open :down :open :done])
;;=> false

(elevator (conj (vec (take 1000000 (interleave (repeat :close) (repeat :open)))) :done))
;;=> true ;; Can work with large number of events as well.
