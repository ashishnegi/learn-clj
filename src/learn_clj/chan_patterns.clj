(ns learn-clj.chan-patterns
  (:require [clojure.core.async :as async]))

;; first create a channel..
(def mychan (async/chan))

;; # principles
;; #1 : taking/putting on a channel blocks if no one is putting/taking from it.
;; => can cause deadlocks if done synchronously.
;; so, do everything asynchronous or max 1 operation synchronous.

(def value 10)
(async/go (async/>! mychan value)) ;; asynchronous
(println "Got value from mychan: " (async/<!! mychan)) ;; 1 operation sync

;; in practical world you would be having async operations only.

;; #2 : close channel : sending returns false.
(def close-chan1 (async/chan))
(async/close! close-chan1) ;; close chan
(= (async/>!! close-chan1 value)
   false)

;; #3 : close channel : receive returns nil.
;; you can not put nil on a channel.
(def close-chan2 (async/chan))
(async/close! close-chan2)
(= (async/<!! close-chan2)
   nil)

;; #4 : How will the go block finish ? : go-routine leaks ??

;; #5 : Don't communicate by sharing memory :=> not possible simply in FPLs.
;; share memory by communicating :=> easier to do for functional programmers :)

;; Point here is not when to use channels.. but how to use them effectively.
;; For when to use : you will know when you have to.
;; most common : server side : Blocking IO (libraries returning channels).

;; Also, your code will be coupled with channels : to much extent.
;; Channels (CSP) is a way to do concurrency : some coupling will be unavoidable.
;; Or look out for how to keep strategies our of data-structure..

;; How code changes ?
;; Here we are dealing about how does the code changes when we want to do async tasks :

;; lets assume we have a blocking work to do ; read files.. blah blah blah
;; for conveniece sake, i use inc here.. so witty.. :P
(def blocking-work +)

(defn work1 [work]
  (map (partial blocking-work 1) work)) ;; inc is blocking work..

(work1 [1 2 3])
;; => (2 3 4)

;; in some sense, channel is also a stream / queue
;; we want to put queues between different subsystems of our program
;; so that each becomes independent => we gain swapability ; easy debugging ; resource allocation.

;; Things to remember : async code always returns channel.
;; above code becomes :
(defn blocking-async [& args]
  (let [ch (async/chan 1)] ;; don't want to block myself if no one is taking.
    (async/go (async/>! ch (apply blocking-work args)))
    ch))

(do ;; Using do so that i can evaluate whole block in one command.. :P
  (defn async-work1 [work-chan]
    (let [out (async/chan)]
      (async/go-loop []
        (let [work (async/<! work-chan)
              res (async/<! (blocking-async 1 work))]
          (async/>! out res)
          (recur)))
      out))

  (defn test1 []
    (let [work-ch (async/chan)
          async-out1 (async-work1 work-ch)]
      ;; async/>!! returns true means we are able to put.
      (async/>!! work-ch 1)
      (assert (= 2 (async/<!! async-out1)))
      (async/>!! work-ch 2)
      (assert (= 3 (async/<!! async-out1)))))

  (test1))

;; Question :
;; what is wrong / or possibly go wrong in async-work1 ?
;; In real world, we spawn go routines (go-loops) for some requests,
;; but forget to clean them ..

;; Thought A : ALWAYS think about how will go routine stops ?

;; Here and usually in real world :
;; We want to stop when there is no more data in work-chan.

(do
  (defn async-work2 [work-chan]
    (let [out (async/chan)]
      (async/go-loop []
        (if-let [work (async/<! work-chan)] ;; nil means work-chan is closed.
          (do
            (async/>! out (async/<! (blocking-async 1 work)))
            (recur))))
      out))

  (defn test2 []
    (let [work-ch (async/chan)
          async-out2 (async-work2 work-ch)]
      ;; async/>!! returns true means we are able to put.
      (async/>!! work-ch 1)
      (assert (= 2 (async/<!! async-out2)))
      (async/>!! work-ch 2)
      (assert (= 3 (async/<!! async-out2)))
      (async/close! work-ch)))

  (test2))

;; Question :
;; What is another problem ?
;; In real world, workflow will be :
;; some-req
;; -> for some task, you put on work-channel
;; -> goes through multiple go-loops ; each a go-routine
;; -> you close work-channel
;; But remeber every async fn returns a channel ;
;; who will close those channels.. ?

;; Principle : Closing a channel is only way to tell that "work" is done..

(do
  (defn async-work3 [work-chan]
    (let [out (async/chan)]
      (async/go-loop []
        (if-let [work (async/<! work-chan)] ;; nil means work-chan is closed.
          (do
            (async/>! out (async/<! (blocking-async 1 work)))
            (recur))
          (async/close! out))) ;; close the out as well. if's else :P
      out))

  (defn test3 []
    (let [work-ch (async/chan)
          async-out3 (async-work3 work-ch)]
      ;; async/>!! returns true means we are able to put.
      (async/>!! work-ch 1)
      (assert (= 2 (async/<!! async-out3)))
      (async/>!! work-ch 2)
      (assert (= 3 (async/<!! async-out3)))
      (async/close! work-ch)
      ;; check whether closing worker-channel closed out-channel ?
      (assert (= nil (async/<!! async-out3)))
      (assert (= false (async/>!! async-out3 1)))))

  (test3))

;; Question : Who closes the channels ?

;; Principle : Sender(s) (those putting data on channel) closes the channel.
;; Usually who creates the channels will be putting values on channel
;; and will be best place to know when work is done.
;; Also, there will be multiple receivers (hopefully), as receivers are workers. Right !!!

;; In some languages, closing a channel multiple times throws.
;; This leads to good programming habits.
;; Not in clojure
(def to-close-chan (async/chan))
(async/close! to-close-chan)
(async/close! to-close-chan)

;; --------------------- PATTERNS ---------------------------------------------
;; inspired from :
