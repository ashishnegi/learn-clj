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
;;11.


;; To do
;; 1. What is symbol keys ? => symbols like 'a can be used as keys
;; 2.
