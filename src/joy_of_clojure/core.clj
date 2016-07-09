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

;; To do
;; 1. What is symbol keys ? => symbols like 'a can be used as keys
;; 2.
