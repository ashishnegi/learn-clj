(ns joy-of-clojure.chap11-parallelism)

(defn- sleeper [thing]
  (java.lang.Thread/sleep 1000)
  thing)

(-> (apply pcalls
           (map #(fn [] (sleeper %))
                (range 100)))
    doall
    time)
;; => (0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50 51 52 53 54 55 56 57 58 59 60 61 62 63 64 65 66 67 68 69 70 71 72 73 74 75 76 77 78 79 80 81 82 83 84 85 86 87 88 89 90 91 92 93 94 95 96 97 98 99)
;; "Elapsed time: 4015.079481 msecs"

(->> (range 100)
     (pmap sleeper)
     doall
     time)
;; => (0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50 51 52 53 54 55 56 57 58 59 60 61 62 63 64 65 66 67 68 69 70 71 72 73 74 75 76 77 78 79 80 81 82 83 84 85 86 87 88 89 90 91 92 93 94 95 96 97 98 99)
;; "Elapsed time: 4008.72929 msecs"

(time (dorun
        (->> (map (fn [_]
                    (future (java.lang.Thread/sleep 1000)))
                  (range 100))
             (map deref))))
;; "Elapsed time: 4007.973986 msecs"

(time (dorun
        (->> (map (fn [_]
                    (future (java.lang.Thread/sleep 1000)))
                  (range 100))
             (mapv deref))))
;; "Elapsed time: 4015.463397 msecs"

(time (dorun
        (->> (mapv (fn [_]
                     (future (java.lang.Thread/sleep 1000)))
                   (range 100))
             (map deref))))
;; "Elapsed time: 1012.666623 msecs"


;; Because of chunkiness of lazy-seq in map
;; things are processed in chunks of ~30.
;; leading to time taken around ~ 100 / 30

;; With mapv : everything is eager and leads to
;; firing all futures simuntaneously. total time is 1 sec.
