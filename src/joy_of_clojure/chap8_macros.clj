(ns joy-of-clojure.chap8-macros)

(macroexpand '(when true true))
;; => (if true (do true))

(defmacro mywhen [condition & body]
  `(when ~condition (do ~@body)))

(macroexpand-1 '(mywhen x true))
;; => (clojure.core/when x (do true))

(macroexpand '(mywhen x true))
;; => (if x (do (do true)))

(let [x 9
      y '(- x)]
  (println `y)
  (println ``y)
  (println `~`y)
  (println `~y)
  (println ``~~y))
;; => nil
;; joy-of-clojure.chap8-macros/y
;; (quote joy-of-clojure.chap8-macros/y)
;; joy-of-clojure.chap8-macros/y
;; (- x)
;; (- x)

;; (domain man-vs-monster
;;         (grouping people
;;                   (Human "A stock human")
;;                   (Man (isa Human)
;;                        "A man, baby"
;;                        [name]
;;                        [has-beard?]))
;;         (grouping monsters
;;                   [Chupacabra
;;                    "A fierce, yet elusive creature"
;;                    [eats-goats]]))

(defn mk-groupings [groups]
  groups)

(defmacro domain [name & body]
  `{:tag :domain
    :attrs {:name "ash"}
    :content [(mk-groupings ~@body)]})

(domain man-vs-monster
        [1])
;; => {:content [[1]], :attrs {:name "ash"}, :tag :domain}
