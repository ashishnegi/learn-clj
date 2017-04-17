(ns learn-clj.chap16-thinking-programs
  (:require [clojure.set :as set]))

(defmacro look [x]
  `(do
     ;; (prn (class ~x))
     (prn #_'~x ~x)
     ~x))

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
    (set/difference '#{1 2 3 4 5 6 7 8 9} occupied-values)))

(defn- solve [maze]
  (if-let [[empty-r empty-c] (first
                               (mapcat
                                 (fn [rth row]
                                   (->> (map (fn [cth v]
                                               (if (= v '-)
                                                 [rth cth]))
                                             (range)
                                             row)
                                        (remove nil?)))
                                 (range)
                                 maze))]
    (->> (possibilities maze empty-r empty-c)
         (mapcat #(solve (assoc-in maze [empty-r empty-c] %)))
         (remove empty?)
         first
         vector)
    [maze]))
