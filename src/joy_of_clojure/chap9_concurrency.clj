(ns joy-of-clojure.chap9-concurrency
  (:import [java.lang Thread]))

(defn slow-fast-stm [r]
  (do
    (let [slow-tries (atom 0)]
      (future
        (dosync
          (swap! slow-tries inc)
          (Thread/sleep 200)
          @r)
        (println (format "@r: %d , history: %d, slow-tries: %d"
                         @r
                         (.getHistoryCount r)
                         @slow-tries))))
    (dotimes [i 500]
      (Thread/sleep 10)
      (dosync
        (alter r inc)))))

(slow-fast-stm (ref 0))
;; @r: 500 , history: 10, slow-tries: 29

(slow-fast-stm (ref 0 :max-history 30))
;; @r: 335 , history: 18, slow-tries: 19

(slow-fast-stm (ref 0 :min-history 15 :max-history 30))
;; @r: 52 , history: 17, slow-tries: 3
