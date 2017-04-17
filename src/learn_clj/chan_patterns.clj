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

;; --------------------- Building blocks ---------------------------------------------
;; inspired from :

;; Building block : Pipeline :
(do
  (def source-ch (async/chan 10))
  ;; cheating ; putting data in source-ch
  (doall (map #(async/>!! source-ch %) (range 10)))
  ;; pipeline : three works one after another ;
  ;; could have been any work
  (def sink-ch
    (-> source-ch
        async-work3 ;; this has source-ch and returns chan-x
        async-work3 ;; this has chan-x and returns chan-y
        async-work3)) ;; this has chan-y and returns sink-ch

  ;; closing the source-ch, stops the process eventually..
  (async/close! source-ch)
  (def all-sink-ch-res
    (loop [v (async/<!! sink-ch)
           res []]
      (if (nil? v) ;; nil means sink-ch got closed / pipeline ended..
        res
        (recur (async/<!! sink-ch)
               (conj res v)))))
  (assert (= all-sink-ch-res
             [3 4 5 6 7 8 9 10 11 12])))

;; so this is looking like calling functions..
;; stack of fn call here is pipeline..
;; the way to return is closing the source-ch..

;; Question :
;; So, how will we return from in-between the pipeline.. ? from the End ?

;; Should the intermediate pipeline-stage eat the error
;; and not send it forward ?
;; OR
;; For exceptional situations :
;; Should we provide a way to stop whole pipeline ? How to do it cleanly ?
;; We can stop the downstream by closing my out-ch.
;; Should i close the upstream chan ? but i am a receiver..

;; adding a done channel
(do
  (def source-ch (async/chan 10))
  (doall (map #(async/>!! source-ch %) (range 10)))
  (defn async-work4 [done work-ch]
    (let [out (async/chan)
          close-out-fn #(async/close! out)]
      (async/go-loop []
        (async/alt!
          done ([v]
                (assert (nil? v)) ;; done is used for only signalling done :P
                (close-out-fn))
          work-ch ([work]
                   (if work
                     (do
                       (async/>! out (async/<! (blocking-async 1 work)))
                       ;; check if it returned true and only recur in that case..
                       (recur))
                     (close-out-fn)))))
      out))

  (def done (async/chan))
  (def sink-ch
    (->> source-ch
         (async-work4 done) ;; this has source-ch and returns chan-x
         (async-work4 done)    ;; this has chan-x and returns chan-y
         (async-work4 done)))      ;; this has chan-y and returns sink-ch

  ;; closing the source-ch, stops the process eventually..
  (async/close! source-ch)

  ;; but sink closes after taking 3 nums..
  ;; this is the code using sink-channel.
  (assert
   (> 6 ;; hopefully should stop before processing more than 6 args.. / can fail
      (count (loop [res []]
               (if-let [v (async/<!! sink-ch)]
                 (do (if (>= (count res) 3)
                       (async/close! done))
                     (recur (conj res v)))
                 res))))))

;; tl;dr : Your async fn need to take done-channel.
;; always close channels before returning.
;; always make sure that your go-loops will end.

;; Building block #2
;; Fan out, Fan in :
;; from one channel -> multiple reads : Fan out..
;; Multiple puts on one channel -> fan in..
(let [ch1 (async/chan 1)
      ch2 (async/chan 1)
      merged-ch (async/merge [ch1 ch2])]
  (async/>!! ch1 "hello")
  (async/close! ch1)
  (async/>!! ch2 "world")
  (println (async/<!! merged-ch))
  (println (async/<!! merged-ch))
  (async/close! ch2)
  ;; if all channels are closed, merged-ch will also be closed.
  (assert (= nil (async/<!! merged-ch))))

;; Building block #3
;; Bounded parallelism:
;; find-grep for a file : file name is number
(let [num-workers 4
      common-work-ch (async/chan num-workers) ;; shared across all workers.
      done-ch (async/chan) ;; yours donely.
      file-to-find 199
      max-files 200] ;; just magic no.. ignore
  (println "\n\n\n**********************")
  (let [look-in-dir-fn (fn [dir]
                         (println "in look-in-dir-fn: " dir)
                         (let [all (->> (iterate (fn [_]
                                                   (int (* (rand) max-files)))
                                                 dir)
                                        (take dir))
                               dirs (->> all
                                         (filter #(>= 100 %)))
                               has-file (->> all
                                             (filter #(= file-to-find %))
                                             empty?
                                             not)]
                           ;; (println "dirs: " dirs)
                           ;; ideally we should return channel as this is most blocking part of code..
                           {:found has-file
                            :dirs dirs}))

        async-work5 (fn [done work-ch]
                      (println ">>>>>>>>> async-work5 <<<<<<<<<<<")
                      (let [out (async/chan 1)
                            close-out-fn #(async/close! out)]
                        (async/go-loop []
                          (let [v (async/alt!
                                    done ([_]
                                          (close-out-fn))
                                    work-ch ([work]
                                             (if work
                                               (let [res (look-in-dir-fn work)]
                                                 (async/alt!
                                                   done ([_]
                                                         (close-out-fn))
                                                   [[out res]] :send))
                                               (close-out-fn))
                                             :recur))]
                            (if (= v :recur)
                              (recur)
                              (println ">>>>> stopping async-work5 instance <<<<<<"))))
                        out))
        all-chans (doall
                   (map (fn [_]
                          {:out-chan (async-work5 done-ch common-work-ch)
                           :in-chan common-work-ch})
                        (range num-workers)))
        out-chan (async/merge (map :out-chan all-chans))
        ;; intermediate channel where each worker result creates a go-routine to put on this.
        intermediate-ch (async/chan num-workers)]

    ;; read out-chan for the result..
    (async/go-loop [reqs-pending 1]
      (if (> reqs-pending 0)
        (do
          (println (str "req-pending: " reqs-pending))
          (if-let [res (async/<! out-chan)]
            (if (:found res)
              (do
                (println "***found***")
                (async/close! done-ch))
              (do
                (println "creating another go-loop for intermediate-ch")
                ;; put on common-work-ch in separate go-routine.
                (async/go-loop
                    [dirs (:dirs res)]
                  (let [v (async/alt!
                            done-ch :done
                            :default :ongoing)]
                    (if (and (= v :ongoing) (seq? dirs))
                      (do
                        (async/alt!
                          done-ch :done
                          [[common-work-ch (first dirs)]] :send)
                        (recur (next dirs)))
                      (println "closing go-loop for intermediate-ch"))))
                (recur (+ reqs-pending (- (count (:dirs res)) 1)))))
            (println "done with out-chan")))
        (println "done with out-chan all reqs done")))

    (async/go-loop
        [seen #{}] ;; don't revist what we have already seen.
      (async/alt!
        done-ch :done
        intermediate-ch ([work]
                         (if work
                           (if-not (contains? seen work)
                             (do
                               (async/alt!
                                 done-ch :done
                                 [[common-work-ch work]] :send)
                               (recur (conj seen work)))
                             (recur seen))))))

    ;; start from 0th directory
    (async/>!! common-work-ch 20)))

;; value more than 199 require a map to stop processing directories
;; we have already seen
