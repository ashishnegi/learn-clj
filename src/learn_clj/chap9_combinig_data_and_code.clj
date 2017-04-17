(ns learn-clj.chap9-combinig-data-and-code)

(defprotocol FIXO
  ;; First In X Out
  (f-push [t v])
  (f-pop [t])
  (f-peek [t]))

(deftype Tree [val l r]
  FIXO
  (f-push [_ v]
    (if (< v val)
      (Tree. val (f-push l v) r)
      (Tree. val l (f-push r v))))
  (f-pop [_]
    (if l
      (Tree. val (f-pop l) r)
      r))
  (f-peek [_]
    (if l
      (f-peek l)
      val))

  clojure.lang.Seqable
  (seq [_]
    (concat (seq l) [val] (seq r)))

  Object
  (toString [t]
    (str (seq t))))

(extend-type
    nil FIXO
    (f-push [_ v]
      (Tree. v nil nil)))

(def tree (Tree. 10 nil nil))
;; => #'learn-clj.chap9-combinig-data-and-code/tree

(seq tree)
;; => (10)

(defn- f-into [t coll]
  (reduce f-push t coll))

(seq (f-into tree (range 10)))
;; => (0 1 2 3 4 5 6 7 8 9 10)
