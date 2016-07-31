(ns joy-of-clojure.chap17-way-of-thinking
  (:require [clojure.string :as str]))

(defn shuffle-expr [expr]
  (if-not (coll? expr)
    (str expr)
    (if (= (first expr) `unquote)
      "?"
      (let [[op & args] expr]
        (str "("
             (str/join (str " " op " ")
                       (map shuffle-expr args))
             ")")))))
