(ns joy-of-clojure.chap16-logic-programming
  (:require [clojure.core.logic :as logic]
            [clojure.core.logic.fd :as fd]
            [clojure.set :as set]))

(def ^:private sudoku-maze
  '[[3 - - - - 5 - 1 -]
    [- 7 - - - 6 - 3 -]
    [1 - - - 9 - - - -]
    [7 - 8 - - - - 9 -]
    [9 - - 4 - 8 - - 2]
    [- 6 - - - - 5 - 1]
    [- - - - 4 - - - 6]
    [- 4 - 7 - - - 2 -]
    [- 2 - 6 - - - - 3]])

(defn- row [maze r]
  (nth maze r))

(defn- col [maze c]
  (mapv #(nth % c) maze))

(defn- square
  "Square box of 3 x 3 of location of element at (r,c)"
  [maze r c]
  (let [start-r (* 3 (int (/ r 3)))
        end-r (+ start-r 3)
        start-c (* 3 (int (/ c 3)))
        end-c (+ start-c 3)]
    (mapcat #(subvec (row maze %) start-c end-c)
            (range start-r end-r))))

(defn- possibilities [maze r c]
  (let [occupied-values (-> (row maze r)
                            (concat (col maze c))
                            (concat (square maze r c))
                            set)]
    (set/difference (set (range 1 10)) occupied-values)))

(defn- all-possibilities [maze]
  (for [r (range (count maze))
        c (range (count (first maze)))]
    (possibilities maze r c)))

(defn- maze->lvars [maze]
  (mapv #(mapv logic/lvar %) maze))

(defn- rows [maze]
  maze)

(defn- cols [maze]
  (apply (partial map vector) maze))

(defn- squares [maze]
  (let [nrows (count maze)
        ncols (count (first maze))]
    (->> (for [r (range 0 nrows 3)
               c (range 0 ncols 3)
               x (range r (+ r 3))
               y (range c (+ c 3))]
           (get-in maze [x y]))
         (partition 9))))

(defn- init [[lvar & lvars] [cell & cells] [vals & valss]]
  (if lvar
    (logic/fresh []
      (if (= '- cell)
        (fd/in lvar (apply fd/domain vals))
        (logic/== lvar cell))
      (init lvars cells valss))
    logic/succeed))

(defn solve [maze]
  (let [lvars-maze (maze->lvars maze)
        lrows (rows lvars-maze)
        lcols (cols lvars-maze)
        lsquares (squares lvars-maze)
        legal-nums (fd/interval 1 9)]
    (logic/run 1 [q]
      (init (flatten lvars-maze) (flatten maze) (all-possibilities maze))
      (logic/everyg #(fd/in % legal-nums) (flatten lvars-maze))
      (logic/everyg fd/distinct lrows)
      (logic/everyg fd/distinct lcols)
      (logic/everyg fd/distinct lsquares)
      (logic/== q lvars-maze))))
