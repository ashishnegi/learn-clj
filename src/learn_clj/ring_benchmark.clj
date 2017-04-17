(ns learn-clj.ring-benchmark
  (:require [clojure.core.async :as async]))

(defn spawn-relay-ch [prev n]
  (if (== n 0)
    prev
    (let [next (async/chan)]
      (async/go-loop []
        (async/>! prev (inc (async/<! next)))
        (recur))
      (recur next (dec n)))))

;; (def startch (async/chan))
;; (def lastch (spawn-relay-ch startch 10))
;; (async/go (println (async/>! lastch 1)))
;; (println (async/<!! startch))

(defn -main [M1 N1]
  (let [M (Integer/parseInt M1)
        N (Integer/parseInt N1)]
    (println "M: " M " N: " N)
    (dotimes [i 10]
      (let [num-messages
            (time
             (let [manager (async/chan)
                   last-ch (spawn-relay-ch manager (dec N))]
                                        ; start things off as it is relay..
               (async/>!! last-ch 1)
               ;; manager will receive after N messages in relay..
               (loop [j (int 1)]
                 (let [m (async/<!! manager)] ;; receive message
                   (if (< j M) ;; receive M messages at most
                     (do
                       ;; to receive next message send on last-ch
                       ;; and we will receive after it goes through N chs.
                       (async/>!! last-ch (inc m))
                       (recur (inc j)))
                     m)))))]
        (println i ": Messages " num-messages)))))

;; learn-clj git:(master) lein trampoline run -m learn-clj.ring-benchmark 1000 1000
;; M:  1000  N:  1000
;; "Elapsed time: 4241.533446 msecs"
;; 0 : Messages  1000000
;; "Elapsed time: 3810.475234 msecs"
;; 1 : Messages  1000000
;; "Elapsed time: 3644.374428 msecs"
;; 2 : Messages  1000000
;; "Elapsed time: 4068.786353 msecs"
;; 3 : Messages  1000000
;; "Elapsed time: 4376.846126 msecs"
;; 4 : Messages  1000000
;; "Elapsed time: 4166.939514 msecs"
;; 5 : Messages  1000000
;; "Elapsed time: 3960.11618 msecs"
;; 6 : Messages  1000000
;; "Elapsed time: 3925.829686 msecs"
;; 7 : Messages  1000000
;; "Elapsed time: 3823.657674 msecs"
;; 8 : Messages  1000000
;; "Elapsed time: 3996.553835 msecs"
;; 9 : Messages  1000000
