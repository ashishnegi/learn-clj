(ns joy-of-clojure.transducers-presentation
  (:require [clojure.core.async :as async]))

;; Hello :)
;; Transducer is a concept and can be applied in any language.
;; a concept can be explained in 2 lines
;; and you can spend 2 days going down the rabbit hole..
;; so .. shall we ??

;; So, what is a transducer ?
;; Uh... it would complicate if i give a definition..

;; so lets see an example..

;; Here is a simple data manipulation by pipelining it through various functions.
(->> (range 1 100) ;; this is a sequence of data
     (map inc)     ;; from here comes our busingess-logic / algorithm
     (filter even?)
     (reduce +))
;; => 2550

;; Above code is actually
(macroexpand '(->> (range 1 100) ;; this is a sequence of data
                   (map inc)
                   (filter even?)
                   (reduce +)))
;; => (reduce + (filter even? (map inc (range 1 100))))

;; so.. what happens if data now comes from a channel.
(def my-chan (async/chan 100)) ;; buffer of 100
;; => #'joy-of-clojure.transducers-presentation/my-chan
(async/go-loop [i 0]
  (async/>! my-chan i)
  (if (< i 100)
    (recur (inc i))))
;; => #object[clojure.core.async.impl.channels.ManyToManyChannel 0xb158039 "clojure.core.async.impl.channels.ManyToManyChannel@b158039"]

;; so now we can do ..
(->> my-chan
     (map inc)
     (filter even?)
     (reduce +))
;; 1. Unhandled java.lang.IllegalArgumentException
;;    Don't know how to create ISeq from:
;;    clojure.core.async.impl.channels.ManyToManyChannel

;;                    RT.java:  542  clojure.lang.RT/seqFrom
;;                    RT.java:  523  clojure.lang.RT/seq
;;                   core.clj:  137  clojure.core/seq
;;                   core.clj: 2637  clojure.core/map/fn

;; Oops.. no.. we can not..

;; create our own map<, filter<, functions for core.async channels.. :(
;; https://github.com/clojure/clojure/blob/master/src/clj/clojure/core.clj#L2700
(clojure.repl/source async/map<)
;; (defn map<
;;   "Deprecated - this function will be removed. Use transducer instead"
;;   [f ch]
;;   .....

;; So.. this is the problem..
;; Our Algorithm is dependent on the source of data..
;; same thing happens with Observables etc..

(clojure.repl/source map)

;; ([f coll]
;;    (lazy-seq
;;     (when-let [s (seq coll)]  ;;  <<<<<<---- this is the dependency..
;;       (if (chunked-seq? s)
;;         (let [c (chunk-first s)
;;               size (int (count c))
;;               b (chunk-buffer size)]
;;           (dotimes [i size]
;;               (chunk-append b (f (.nth c i))))
;;           (chunk-cons (chunk b) (map f (chunk-rest s))))
;;         (cons (f (first s)) (map f (rest s)))))))


;; So what can we do.. ??
;; We do not want `map` to know about collection..
;; and it is not just about map.. filter/partition/take.. all of them..
;; or may be the "transducer" that you and me will create..
;; how do we force others to not call any function or touch the collection..

;; well easiest way is to NOT EVEN TAKE data collection..
;; so how will map work.. ??

;; so.. we know that map can be written in terms of reduce
(defn my-map [f coll]
  (reduce (fn [res val]
            (conj res (f val)))
          []
          coll))
;; => #'joy-of-clojure.transducers-presentation/my-map
(= (my-map inc (range 100))
   (map    inc (range 100)))
;; => true

;; so remove collection from map
(defn my-map2
  [f]
  (reduce (fn [res val]
            (conj res (f val)))
          []
          ;;... what comes here ??
          ))

;; so what is map actually ?
;; we want to call a function over each element of collection..
;; this is happens at (f val)

(defn my-map3
  [f]
  (fn [res val]
    (conj res (f val))))

;; how to use this map ??
(let [mapxf (my-map3 inc)]
  (reduce mapxf [] (range 100)))
;; => [1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50 51 52 53 54 55 56 57 58 59 60 61 62 63 64 65 66 67 68 69 70 71 72 73 74 75 76 77 78 79 80 81 82 83 84 85 86 87 88 89 90 91 92 93 94 95 96 97 98 99 100]

;; so Now job of running the loop is outside of map.. :)

;; so can we now use this new "map" ??
;; no because there is `conj` ... :( in my-map3
;; and conj means we are still thinking in terms of sequences..
;; you cannot conj to a channel..

;; sooo.. ?
;; remove conj.. :)

;; remember conj is a reducing function..
;; i call something whose signature is (fn [result-till-now new-val] ...) a reducing function..
(defn my-map4
  [f]
  (fn [rf]
    (fn [res val]
      (rf res (f val)))))

(let [mapxfc (my-map4 inc)
      mapxf (mapxfc +)]
  (reduce mapxf 0 (range 100)))
;; => 5050

;; It workss. .... !!!! :) :)

;; now if we look carefully.. `res` is result-till-now..
;; and `val` is new value..
;; and nobody is touching them.. calling any fn on them..
;; expect externally supplied `rf` and `f`

;; but now we need to make a function that take a collection/chan
;; and reduce over it..
;; i.e.  (reduce my-transducer init-value collection)

;; so where does the init value comes from ??
;; does not compile..
(defn my-transduce [xform rf source]
  (let [f (xform rf)]
    (reduce f init-value source)))

;; where will the init-value comes from ??
(defn my-transduce [xform rf source]
  (let [f (xform rf)]
    (reduce f (f) source)))

(defn my-map5
  [f]
  (fn [rf]
    (fn
      ([res val]
       (rf res (f val)))
      ([] (rf)))))

(my-transduce (my-map5 inc)
              +
              (range 100))
;; => 5050

;; actually ..
(+)
;; => 0
;; :)

(defn my-map-final
  [f]
  (fn [rf]
    (fn
      ([] (rf))
      ([res val]
       (rf res (f val))))))

(defn my-filter-final
  [f]
  (fn [rf]
    (fn
      ([] (rf))
      ([res val]
       (if (f val)
         (rf res val)
         res)))))

(my-transduce (my-filter-final even?)
              conj
              (range 10))
;; => [0 2 4 6 8]

;; Oh.. and what is transducer ???
;; This part is ...
    ;; (fn
    ;;   ([] (rf))
    ;;   ([res val]
    ;;    (if (f val)
    ;;      (rf res val)
    ;;      res)))

;; How this works with channels..
(def chan-2 (async/chan 100 (my-filter-final even?)))
;; => #'joy-of-clojure.transducers-presentation/chan-2
(async/onto-chan chan-2 (range 100))

(->> chan-2
     (async/into [])
     (async/<!!)
     (reduce +))
;; => 2450

;; So what happened now ??

;; At the point of writing algorithm i did not know about data-strucutre..
;; so my `filter` and `map` are agnostic of that..
;; only when i have my `seq` or `chan` i call specific fns
;; and pass my algorithm in specific way..

;; XX -- transducer FTW -- XX


(my-transduce (comp (my-filter-final even?)
                    (my-map-final inc))
              +
              (range 5))
;; => 9
;; is this right answer ??

(my-transduce (comp (filter even?)
                    (map inc))
              +
              (range 5))

;; => 9

;; ((comp f g) x) => (f (g x))
;; what is x in case of transducer ??

;; lets go back to the transducer ..
(defn my-transduce [xform rf source]
  (let [f (xform rf)]               ;; this line is important
    (reduce f (f) source)))

(my-transduce
  (comp (filter even?) (map inc)) ;; this is xform
  + ;; this is rf
  (range 5) ;; this is source
  )

;; concentrate on xform and rf
(def filterxfc (filter even?))
(def mapxfc (map inc))

(def xform (comp filterxfc mapxfc))
;; => #'joy-of-clojure.transducers-presentation/rf
(def rf +)
;; => #function[clojure.core/filter$fn--4808$fn--4809]
(xform rf)
;; => #function[clojure.core/filter$fn--4808$fn--4809]
(xform rf)
;; is
(def f (filterxfc
         (mapxfc +)))

;; this f is at line no 254..
(def mapxf (mapxfc +))
(def filterxf (filterxfc mapxf))

;; this means filterxf == f
;; if i call `f` filter is called first..

;; so transducer composition works in direction
;; <<<<--------------

;; normal fns composition works in direction
;; ------------>>>>>>>>

;; and this is not different behavior of `comp`
;; this is Sparta.. err.. Transducer..

;; XX -- tranducers for more win .. -- XX
;; transducer are fast .. why ??
;; because your whole algorithm is not compressed into one function..
;; intermediate sequences are not constructed.. really big improvement..

(let [v (range 10000)]
  (time (dotimes [i 1000]
          (transduce (comp (map inc)
                           (filter even?)
                           (map (partial + 2)))
                     +
                     v))))
;; "Elapsed time: 672.743663 msecs"

(let [v (range 10000)]
  (time (dotimes [i 1000]
          (->> v
               (map inc)
               (filter even?)
               (map (partial + 2))
               (reduce +)))))
;; "Elapsed time: 915.336987 msecs"

;; and NOTICE just change `->>` to `comp`
;; and done.. :) performance of 35 %..
;; means less boxes..

;; XX -- transducer are statefull.. what.. ? -- XX
;; otherwise how would `take` be implemented.. ?
;; because looping is not in our hand..

;; for cleaning : at the end : we need 1-arity signature in tranducer also..
;; https://github.com/clojure/clojure/blob/master/src/clj/clojure/core.clj#L7045

;; XX -- final win .. with some links.. ok -- XX
;; Transduce : https://github.com/clojure/clojure/blob/master/src/clj/clojure/core.clj#L6746
;; map : https://github.com/clojure/clojure/blob/master/src/clj/clojure/core.clj#L2700
;; take : https://github.com/clojure/clojure/blob/master/src/clj/clojure/core.clj#L2834
;; http://clojure.org/reference/transducers
;; http://kukuruku.co/hub/funcprog/clojure-transducers-reducers-and-other-stuff

;; and if you are thinking what are "trandformers" ..
;; they were rebranded to "transducers"
