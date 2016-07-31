(ns joy-of-clojure.core)

;; Tips here

;; 1. clojure.java.javadoc/javadoc gets you the docs for java apis.
;; 2. Exceptions :
;;    If macros caused a problem we would see : Compiler.java a lot
;;    in stack traces.
;; 3. (rest [])
;;    ;;=> ()
;;    (next [])
;;    ;;=> nil
;;    Use rest to get the rest of the values.
;;    seq is good for checking the emptyness or nil of a sequence.
;;    (seq [])
;;    ;;=> nil
;;    (seq nil)
;;    ;=> nil
;; 4. Namespace symbols can not be referenced independently.
;;    like in repl clojure.set will throw.
;; 5. Their is :require, :refer, :use and :rename
;; 6. Integer overflows throw in clojure. Use `unchecked-add` for not-throw (do not use much.)
;; 7. Keywords do not belong to any namespace.
;;    in repl :   :a  will print only :a and not like :user/a
;;    though ::a will give :user/a  but still it is not bound to the namespace.
;; 8. Symbols also do not belong to any namespace. It is qualified in that namespace because of
;;    its evaluation.
;; 9. Making a lazy function :
;;    a) wrap in `lazy-seq`
;;    b) Use rest and not next
;;    c) use functions (map etc..) returning lazy-seq
;;    d) do not hold head of lazy-sequence.
;;10. Delay and Force macros are their for lazy-evaluation.
;;11. syntax-quote (`) qualifies (resolves) the symbols to namespace
;;    quote (') stops the evalution.
;;    symbols are things at compile time (for macros).
;;    symbols resolve to vars.
;;    vars are things at run time.
;;    vars derefs to actual things.
;;    unquote (~) evaluates the expression at macro time.
;;       needs to be in a (`).
;;    `'~v means evaluate (i.e. ~) v at macro time.
;;               then place (i.e. ') the evaluation at this position.
;;               resolve (`) to its namespace at macro compile time.
;;12. clojure data structures are in 3 categories:
;;       maps, sets, sequentials
;;    sequentials : collected that does not change the order as they were inserted.
;;    sequence : sequential collection having evaluated/unevaluated values.
;;13. `defmulti` , `defmethod` : dispatch (polymorphism) functions arbitraly.
;;    `juxt` is good for defmulti dispatch logic.
;;    `derive` is used as inheritence for reusing implementations.
;;    `prefer-method` for favouring same method over multiple derivations.
;;
;;    `defprotocol` : dispatch based on first argument (fast)
;;    `extend`, `extend-type`, `extend-protocol` : extends type for a protocol.
;;    `defrecord` : creates class, map like structure, extends map-funcs and fast looks.
;;    `deftype` : simple class, does not extend anything.
;;
;;    With defrecord it is not possible to derive map-funcs as they are alredy derived.
;;    deftype and defrecord needs to be `:import` into other namespaces.
;;    defrecords are never = to maps, they are not functions over keys unlike map.
;;
;;    Argument order independence : use (defn foo [& {:keys [a b c d]}] [a b c d])
;;    => (foo :a 1 :c 3 :b 2) => [1 2 3 nil]
;;14. Concurrency :
;;    `ref` : coordinated, synchronous, retriable
;;    `agents` : uncoordinated, asynchronous
;;    `atom` : uncoordinated, synchronous, retriable
;;    `vars` : thread-local, dynamic
;;    coordinated : transaction support while changing multiple; either all or none.
;;
;;    `validator-fn` : validate the chagne in value.
;;    clojure has one transaction per thread at a time.
;;        retry of sub-transaction causes retry of outermost.
;;    `io!` : checks if io is in some transaction.
;;   `commute` : for faster commutative transaction.
;;
;;    stm : `dosync` : change multiple refs or agents;
;;        atoms in dosync will not be reset during retries; fns may be called multiple times.
;;
;;    ref : alter, commute
;;    agent : have queue for fns to change val ; runs in one thread at a time.
;;          : send uses thread from thread-pool; use send for non-blocking fns;
;;                 agent puts itself in thread-wait queue to get thread. good for cpu-tasks
;;          : send-off uses thread from unbounded-pool. good for IO
;;          : erlang style : but with in-process immutable data sharing;
;;          : :fail, :continue, agent-error, set-error-handler!, restart-agent, set-error-model!
;;    vars : unlike other reference objects evaluate to themselves.
;;         : to get the symbol use `var`.
;;         : binding uses stack for latest values (per thread basis).
;;    binding : laziness and binding does not go hand in hand.
;;            : with side-effects its bad; should evaluate completly before leaving binding form.
;;            : `binding-fn` put things in same thread binding form as where it was written.
;;    usage : vars > atom > agent / ref
;;15. parallelism:
;;    `future` and `promise`
;;     future : evaluates expression in another thread.
;;            : future, future-cancel, future-done?, future-cancelled?
;;     promise: delivered by somebody (thread) else.
;;            : write once; again write => exception.
;;    `pvalues` : lazy-seq of parallel execution of expressions with sliding window.
;;    `pmap` : parallel map
;;    `pcalls`: lazy-seq of parallel exectuion of functions in sliding window.
;;16. `proxy` : class on demand : proxing methods that can be changed dynamically.
;;            : construct-proxy, init-proxy, get-proxy-class
;;            : can not be extended further.. use `gen-class`
;;17. primitive-arrays : `into-array` arary of same value types.
;;           : `char-array`, `byte-arary`, `object-array` etc.
;;           : these arrays are mutable , do not share.
;;           : `to-array`, `to-array-2d` are autoboxed.
;;           : creating seq and then sharing is ok as no way to get back the array to change.
;;           : `[B` : primitive byte array, `[I` primitive int array. etc.
;;18. Clojure functions implement Callable, Comparator, Runnable.
;;     : can pass clojure-fns as java-fns, can pass in comparison (<, >) , can pass to threads.
;;19. Clojure data-structures implement:
;;     : sequential colls conform to immutable parts of `java.util.List`.
;;     : vectors implement Comparable and RandomAccess : compare itself to other
;;                                        and for optimized algos respectively.
;;20. `definterfaces` : primitive hints are supported in arguments and return value.
;;21. Exceptions and Errors : `RuntimeException` and `StackOverflowError`.
;;      error when can not continue.
;;      exception when may or may not continue.
;;    Macros throw compile time exception.
;;      `ex-data` and `ex-info` can be used for more information other than stack-trace. ExceptionInfo.
;;22. Code as Code, Data as Data : Easiness to access data from code, keeping information, no PLOP.
;;    Data as Data : Immutable data -> information, regeneration, reasonable, sharable.
;;                 : Flatness, easy Simulation testing.
;;                 : Edn : tagged literals : `*data-readers*`, `read-string`, `*default-data-reader-fn*`.
;;   Data as Code  : Input in data form derives the behavior of app.
;;                 : data-programmable engine.
;;                 : event driven system : event as data -> persisted -> replayed to generate state.
;;                     : essential-state : direct mapping in event model.
;;                     : derived-state   : mapping created from code. (change/versioning is problem)
;;  Code as Data as Code : Macros : Take code and process them as data and spit out more code.
;;                 : Write DSL for your problem domain.
;;23. Performance :
;;       Type hints : for classes and not for primitives. ^floats, ^ints, ^longs
;;            : arguments and return in function declration as well in calling.
;;       Transients : `transient` , `conj!`, `persistent`. Mutating thread only view.
;;       Chunked Seqs : reducing the cost of lazyness. 32 chunks are created at a time.
;;            : created 1-at-a-time with `cons` and `lazy-seq`.
;;       Memoization : `memoize`. but need to create our own `atom` cache if need more flexibility.
;;       Coercion : primitives casting using `(long x)`,
;;                : happens automatically if used in fn-argument ^long.
;;                : `*unchecked-math*` : true => fast but wrong result : negative factorials..
;;                : use `double` => fast but less accuracy.
;;                : auto-promotion : use `+'` ' functions => little slow but accurate. (BigInt)
;;      Reducers (Reducible transformers) / Transducers / parallel fold.
;;24. Way of Thinking:
;;        Planning, interaction with domain experts and potential users, and
;;        deep thinking must come before testing begins. No amount of testing
;;        can substitute for thoroughly thinking through the implications of
;;        potential designs. One interesting way to tease out the requirements
;;        and shape of a solution is to explore its fundamental expectations.
;;
;;        Observer pattern : add-watch to refs.





;;
;; To do
;; 1. What is symbol keys ? => symbols like 'a can be used as keys
;; 2.
