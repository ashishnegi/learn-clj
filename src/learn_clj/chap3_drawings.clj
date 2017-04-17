(ns learn-clj.chap3-drawings)

;; Before running the below code, resize your editor window to 80 % of
;; the screen towards the right side. Otherwie, we would not be able to
;; see the drawing-boards's frame
;; If you can not see the frame while drawing - run
;; (.toFront frame)

(def frame (new java.awt.Frame))

(defn- find-methods
  "Find function-names of the class matching the regex."
  [clas regex]
  (for [method (.getMethods clas)
        :let [name (.getName method)]
        :when (re-find regex name)]
    name))

(find-methods (class frame) #"Vis")
;;=> ("setVisible" "isVisible")

(.setVisible frame true)

(find-methods (class frame) #"size")
;;=> ("size" "resize" "resize" "getBaselineResizeBehavior")
(find-methods (class frame) #"Size")
;;=> ("setSize" "setSize" "setMinimumSize" "getPreferredSize" "getMinimumSize" "preferredSize" "minimumSize" "getMaximumSize" "getSize" "getSize" "setPreferredSize" "setMaximumSize" "isMinimumSizeSet" "isPreferredSizeSet" "isMaximumSizeSet")

(.setSize frame (new java.awt.Dimension 200 200))

(find-methods (class frame) #"Graphics")
;;=> ("getGraphics" "getGraphicsConfiguration")

(def gfx (.getGraphics frame))

(defn- pattern [pattern-fn width height]
  (for [w (range width)
        h (range height)]
    [w h (rem (pattern-fn w h) 256)]))

(find-methods (class gfx) #"clear")
;; ("clearRect")

(defn- draw-pattern [pattern-fn width height]
  (.setSize frame (new java.awt.Dimension width height))
  ;; Need to take new Graphics handler after changing the size
  ;; Otherwise drawing happens on only the starting dimensions.
  (let [gfx (.getGraphics frame)]
    (.clearRect gfx 0 0 width height)
    (doseq [[x y v] (pattern pattern-fn width height)]
      (.setColor gfx (new java.awt.Color v v v))
      (.fillRect gfx x y 1 1))))

(draw-pattern bit-xor 200 200)

(find-methods (class frame) #"Front")
;;=> ("toFront")

(.toFront frame)

(draw-pattern #(and %1 %2) 200 200)

(draw-pattern #(or %1 %2) 200 200)

(draw-pattern + 200 200)

;; Awesomeness :)
(draw-pattern * 800 800)
