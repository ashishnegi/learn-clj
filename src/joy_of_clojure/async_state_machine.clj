(ns joy-of-clojure.async-state-machine
  (:require [clojure.core.async :as async]))

(def firstAsync
  '(async/go
     (async/<! (async/timeout 100))
     (println "hello world")))

(clojure.pprint/write
 (macroexpand firstAsync)
 :dispatch clojure.pprint/code-dispatch)

(comment
  ;; for referece
  ;; (def ^:const FN-IDX 0)
  ;; (def ^:const STATE-IDX 1)
  ;; (def ^:const VALUE-IDX 2)
  ;; (def ^:const BINDINGS-IDX 3)
  ;; (def ^:const EXCEPTION-FRAMES 4)
  ;; (def ^:const CURRENT-EXCEPTION 5)
  ;; (def ^:const USER-START-IDX 6)
  (let*
      ;; creates a channel with 1 buffer ? why 1 buffer ?
      [initial_channel (clojure.core.async/chan 1) ;; renamed from c__5790__auto__
       ;; get all the bindings in thread  ;; renamed from captured-bindings__5791__auto__
       captured-bindings__initial (clojure.lang.Var/getThreadBindingFrame)]
    ;; run in a threadpool
    ;; func in clojure have Runnable interface.
    ;; look for Runnable :
    ;; https://github.com/clojure/clojure/blob/master/src/jvm/clojure/lang/IFn.java#L23
    (clojure.core.async.impl.dispatch/run
      ;; this fn called immediately.
      #(clojure.core/let
           [f__state_machine ;; this is state-machine-func ;; i renamed this from : f__5792__auto__
            (clojure.core/fn state-machine__5579__auto__
              ;; two arities
              ;; 0 arity fn : for initialization;
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
                      ;; reset only 3rd position from state.. ;; BINDING-IDX
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
                             (clojure.core/case
                                 (clojure.core/int
                                  ;; case on 1st position of array. ;; STATE-IDX
                                  (clojure.core.async.impl.ioc-macros/aget-object
                                   current_state
                                   1))
                               ;; if its 1
                               1
                               ;; found our code :)
                               (clojure.core/let [channel_18882 (async/timeout 1000)]
                                 ;; returns :recur keyword if able to take from channel
                                 ;; otherwise nil
                                 (println "here after async/timeout's return value..")
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
         ;; runs the function :
         (clojure.core.async.impl.ioc-macros/run-state-machine-wrapped state__5793__auto__)))
    ;; returns initial channel which should have last value.
    initial_channel))



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
