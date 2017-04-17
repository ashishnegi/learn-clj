(ns learn-clj.chap9-concurrency
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

(defn slow-fast-stm-updates
  [r r2]
  (do
    (let [slow-tries (atom 0)]
      (future
        (dosync
          (swap! slow-tries inc)
          (Thread/sleep 200)
          (alter r2 (constantly @r)))
        (println (format "@r: %d , history: %d, slow-tries: %d"
                         @r
                         (.getHistoryCount r)
                         @slow-tries))))
    (dotimes [i 500]
      (Thread/sleep 10)
      (dosync
        (alter r inc)))))

(def r2 (ref 0 :min-history 15 :max-history 30))

(slow-fast-stm-updates (ref 0 :min-history 15 :max-history 30)
                       r2)
;; @r: 70 , history: 18, slow-tries: 4
@r2
;; => 52

(defn slow-fast-stm-dependency
  [r r2]
  (do
    (let [slow-tries (atom 0)]
      (future
        (dosync
          (swap! slow-tries inc)
          (Thread/sleep 200)
          (alter r inc)
          (alter r2 (constantly @r)))
        (println (format "@r: %d , history: %d, slow-tries: %d"
                         @r
                         (.getHistoryCount r)
                         @slow-tries))))
    (dotimes [i 500]
      (Thread/sleep 10)
      (dosync
        (alter r inc)))))

(def r3 (ref 0 :min-history 15 :max-history 30))

(slow-fast-stm-dependency
  (ref 0 :min-history 15 :max-history 30)
  r3)
;; @r: 501 , history: 18, slow-tries: 30
;; @r: 501 , history: 18, slow-tries: 30
;; @r: 501 , history: 19, slow-tries: 30

;; http://stackoverflow.com/questions/21966319/deref-inside-a-transaction-may-trigger-a-retry-what-is-the-role-of-ref-state-h
;; Clojure's STM does not care about the present. By the time an observation is made,
;; the present has already moved. Clojure's STM only cares about capturing a consistent
;; snapshot of state.
;; This is not very obvious from the example because we know a single read would
;; always be a consistent snapshot. But, if you are only ever using dosync on a
;; single ref, then you probably shouldn't be using refs at all, but atoms instead.

;; So, imagine instead we are reading from an a and a b and trying to return
;; their sum. We don't care that a and b are current when we return the sum --
;; trying to keep up with the present is futile. All we are about is that
;; a and b are from a consistent period of time.

;; If while in a dosync block, we read a and then b but b was updated in between
;; the two reads, we have an a and b from inconsistent points in time. We have
;; to try again -- start all over again and try to read a then b from the near present.

;; Unless... Suppose we kept a history of b for every change to b. As before, suppose
;; we read a and then b but an update to b occurs before we're done. Since we saved
;; a history of b, we can go back in time to before b changed and find a consistent
;; a and b. Then, with a consistent a and b from the near past, we can return a
;; consistent sum. We don't have to retry (and potentially fail again) with new
;; values from the near present.
