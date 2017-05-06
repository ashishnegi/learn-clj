;; state the timer..
;; clean buffer: C-u C-c C-o

(ns learn-clj.async-state-machine
  (:require [clojure.core.async :as async]))

;; async : library for working with asynchronous jobs.
;; follows CSP i.e. uses channels..
;; You write your code still in sync way.. saves us from callback hell and
;; similar problem of dealing with exceptions in async way..
;; but it is executed in async way..
;; best of both worlds..
(async/go
  (try
    (async/<! (async/timeout 1000))
    (throw (Exception. "async exceptions.."))
    (println "hello")
    (catch Throwable t
      (println "caught" t))))

(def firstAsync
  '(async/go
     (async/<! (async/timeout 100))
     (println "hello world")))

;; (clojure.pprint/write
;;  (macroexpand firstAsync)
;;  :dispatch clojure.pprint/code-dispatch)

;;async/go returns a channel which has last value of returned from go-block..
(do
  (def x-chan
    (async/go
      (async/<! (async/timeout 100))
      (println "hello world")
      1))
  (= 1 (async/<!! x-chan)))

;; state machine is everywhere..
;; state machine is nice way to structure your decisions (if-else cases)..
;; if this happens do that and then that if something else happens.. so on..

;; go through the simple code and show emit-state-machine function..

(comment
  ;; for referece
  ;; (def ^:const FN-IDX 0) ;; stores function at 0th position..
  ;; (def ^:const STATE-IDX 1) ;; stores state of machine at 1st position..
  ;; (def ^:const VALUE-IDX 2) ;; stores return value at 2nd..
  ;; (def ^:const BINDINGS-IDX 3) ;; bindings..
  ;; (def ^:const EXCEPTION-FRAMES 4)
  ;; (def ^:const CURRENT-EXCEPTION 5)
  ;; (def ^:const USER-START-IDX 6) ;; initial channel index

  ;; ## open async.clj
  (let*
      ;; creates a channel with 1 buffer ? why 1 buffer ?
      [initial_channel (clojure.core.async/chan 1) ;; renamed from c__5790__auto__
       ;; get all the bindings in thread  ;; renamed from captured-bindings__5791__auto__
       captured-bindings__initial (clojure.lang.Var/getThreadBindingFrame)]
    ;; run in a threadpool
    ;; func in clojure have Runnable interface.
    ;; look for Runnable :
    ;; https://github.com/clojure/clojure/blob/master/src/jvm/clojure/lang/IFn.java#L23
    ;; ## open dispatch.clj
    (clojure.core.async.impl.dispatch/run
      ;; this fn called immediately..
      ;; this is anonymous fn i.e. IFn i.e. Runnable..
      #(clojure.core/let
           [f__state_machine ;; this is state-machine-func ;; i renamed this from : f__5792__auto__
            (clojure.core/fn state-machine__5579__auto__
              ;; two arities
              ;; 0 arity fn : for initialization;

              ;; ## open ioc_macros.clj emit-state-machine fn.
              ([]
               (clojure.core.async.impl.ioc-macros/aset-all!
                ;; constructor : AtomicReferenceArray(int length)
                ;; Creates a new AtomicReferenceArray of the given length, : here 8
                ;; with all elements initially null.
                (java.util.concurrent.atomic.AtomicReferenceArray. 8)
                0 ;; 0th position in array ;; FN-IDX
                state-machine__5579__auto__ ;; set the func
                1 ;; first position in array ;; STATE-IDX
                1))

              ;; 1 arity fn
              ([current_state] ;; state_18887
               (clojure.core/let
                   [;; store the current thread bindings.
                    old-frame__5580__auto__ (clojure.lang.Var/getThreadBindingFrame)
                    ret-value__5581__auto__
                    (try ;; catch all exceptions to revert back old bindings..
                      ;; reset bindings from 3rd (BINDING-IDX) position from state..
                      (clojure.lang.Var/resetThreadBindingFrame
                       (clojure.core.async.impl.ioc-macros/aget-object
                        current_state
                        3))
                      ;; loop in state machine..
                      (clojure.core/loop
                          []
                        (clojure.core/let
                            [;; result of case below ;
                             ;; i.e. which state we are in.
                             result__5582__auto__
                             ;; now is the actual state machine..
                             ;; casing on which state we are in ..
                             ;; and taking appropriate next steps..
                             (clojure.core/case
                                 (clojure.core/int
                                  ;; case on 1st position of array. ;; STATE-IDX
                                  (clojure.core.async.impl.ioc-macros/aget-object
                                   current_state
                                   1))
                               ;; if its 1
                               1
                               ;; found our code :) ;; open in another buffer same file and at the code..
                               (clojure.core/let [channel_18882 (async/timeout 1000)]
                                 ;; returns :recur keyword if able to take from channel
                                 ;; otherwise nil
                                 (clojure.core.async.impl.ioc-macros/take! current_state
                                                                           2 ;; next state value
                                                                           channel_18882))
                               2
                               (clojure.core/let
                                   [inst_18884 (clojure.core.async.impl.ioc-macros/aget-object
                                                current_state
                                                2) ;; value-idx
                                    code_result (println "hello world") ;; found our code..
                                    current_state (clojure.core.async.impl.ioc-macros/aset-all!
                                                   current_state
                                                   7
                                                   inst_18884)]
                                 ;; returns a channel set in user-start-idx
                                 ;; after putting result of our code and closing it.
                                 (clojure.core.async.impl.ioc-macros/return-chan current_state
                                                                                 code_result)))]
                          ;; if state machine told to recur then recur else return result.
                          (if
                              (clojure.core/identical? result__5582__auto__
                                                       :recur)
                            (recur)
                            result__5582__auto__)))
                      (catch
                          java.lang.Throwable
                          ex__5583__auto__
                        (clojure.core.async.impl.ioc-macros/aset-all!
                         current_state
                         2 ;; value-idx set to exception.
                         ex__5583__auto__)
                        ;; if our exception frames are not empty..
                        ;; set next state with first exception frame..
                        (if
                            (clojure.core/seq
                             (clojure.core.async.impl.ioc-macros/aget-object
                              current_state
                              4)) ;; 4 is exception frames.
                          (clojure.core.async.impl.ioc-macros/aset-all!
                           current_state
                           1
                           (clojure.core/first
                            (clojure.core.async.impl.ioc-macros/aget-object
                             current_state
                             4))
                           4
                           (clojure.core/rest
                            (clojure.core.async.impl.ioc-macros/aget-object
                             current_state
                             4)))
                          (throw
                           ex__5583__auto__))
                        :recur)
                      ;; reset to old bindings.
                      (finally
                        (clojure.lang.Var/resetThreadBindingFrame
                         old-frame__5580__auto__)))]
                 ;; at this time.. we have run the state machine's one loop and
                 ;; we are at next state : 2..
                 ;; if return-value of stepping through state machine is :recur then recur
                 (if (clojure.core/identical? ret-value__5581__auto__ :recur)
                   (recur current_state)
                   ;; otherwise return the last value given by state machine which is channel.
                   ret-value__5581__auto__))))

            ;; another variable in let
            state__5793__auto__
            (clojure.core/-> (f__state_machine) ;; 0 arity call
                             (clojure.core.async.impl.ioc-macros/aset-all!
                              clojure.core.async.impl.ioc-macros/USER-START-IDX
                              initial_channel
                              clojure.core.async.impl.ioc-macros/BINDINGS-IDX
                              captured-bindings__initial))]
         ;; runs the function : remember : all this is happening in another thread...
         (clojure.core.async.impl.ioc-macros/run-state-machine-wrapped state__5793__auto__)))
    ;; current thread on which we started go-routine, we will return channel..
    ;; returns initial channel which should have last value.
    initial_channel))

;; who calls the state machine again after timeout..
;; look at take!.. we registered a fn that will be called by channel.. when it has some value..
;; so actually it is still callbacks.. :P
;; but some one else has written the code for you..
;; go to channels.clj#L239 https://github.com/clojure/core.async/blob/master/src/main/clojure/clojure/core/async/impl/channels.clj#L239

;; Thread Main : MainBindings
;; run a func in threadpool and return initial-channel

;; Thread go-1 :
;;   Fn-idx                   state-idx        user-start-idx      bindings-idx
;; statemachine              1              initial-channel         mainbindings    : state-1
;;
;; run-state-machine-wrapped
;; (fn-idx state-1)
;; state-idx is 1

;; case 1 works :
;; take! looks into timeout-channel and puts a handler in its list of takers :
;;  to fire when it gets the value.
;; https://github.com/clojure/core.async/blob/master/src/main/clojure/clojure/core/async/impl/channels.clj#L239

;; the handler calls run-state-machine-wrapped after setting the state-idx value to 2.
;; we come to print. We go to return-chan() which returns initial-channel after
;; puting value of last statement into it andclosing it.

;; https://github.com/clojure/core.async/blob/master/src/main/clojure/clojure/core/async/impl/ioc_macros.clj#L912
;; https://github.com/clojure/core.async/blob/master/src/main/clojure/clojure/core/async/impl/ioc_macros.clj#L935

;; In compiler design, static single assignment form (often abbreviated as SSA form or simply SSA)
;; is a property of an intermediate representation (IR), which requires that each variable is
;; assigned exactly once, and every variable is defined before it is used.

;; some benchmarks of implementation:
;; in learn-clj
;; lein trampoline run -m learn-clj.ring-benchmark 1000 1000
;; in pulsar
;; lein trampoline run -m co.paralleluniverse.pulsar.examples.primitive-ring-benchmark 1000 1000
;; but remember that mostly we will be using async for slow io works..
;; so it does not matter even if clojure impl is slower..
