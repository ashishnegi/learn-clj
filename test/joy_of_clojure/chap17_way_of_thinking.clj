(ns learn-clj.chap17-way-of-thinking
  (:require [learn-clj.chap17-way-of-thinking :as sut]
            [clojure.test :as t]))

(t/deftest shuffle-expr-test
  (t/testing "simple values"
    (t/is (= "42" (sut/shuffle-expr 42))))

  (t/testing "(shuffle-expr `(unquote max))"
    (t/is (= "?" (sut/shuffle-expr `(unquote max)))))

  (t/testing "prefix to infix notation"
    (t/is (= "(X.a = Y.b)" (sut/shuffle-expr '(= X.a Y.b)))))

  (t/testing "nested operators expression"
    (t/is (= "((a < 5) AND (b < ?))" (shuffle-expr '(AND (< a 5) (< b ~max))))))

  (t/testing "complex expression"
    (t/is (= "((a < 5) AND ((b > 0) OR (b < ?)))" (shuffle-expr '(AND (< a 5) (OR (> b 0) (< b ~max))))))))


(shuffle-expr-test)
