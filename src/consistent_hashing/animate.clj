(ns consistent-hashing.animate
  (:require [consistent-hashing.core :as core])
  (:import [javax.swing JFrame JLabel]
           [java.awt.image BufferedImage]
           [java.awt Graphics Dimension Color]
           [gifAnimation GifEncoder]))

(defn paint-canvas [size graphics caches assignment]

  ;; draw the unit circle
  (.setColor graphics (Color. 255 255 255))
  (.fillRect graphics 0 0 (+ 10 size) (+ 10 size))
  (doseq [[c x] caches]
    (.setColor graphics (Color. 255 0 0))
    (when (assignment c)
      (let [diam (+ 10 (/ (assignment c)
                          250))]
        (.fillOval graphics
                   (+
                    (/ size 2)
                    (- (/ diam 2))
                    (int (* (/ size 2) (Math/cos (* x
                                                    2
                                                    Math/PI)))))
                   (+
                    (/ size 2)
                    (- (/ diam 2))
                    (int (* (/ size 2) (Math/sin (* x
                                                    2
                                                    Math/PI)))))
                   diam
                   diam))))
  (.drawOval graphics
             0
             0
             size
             size))

(defn draw [size caches assignment]
  (let [image  (BufferedImage. (+ 10 size) (+ 10 size) BufferedImage/TYPE_INT_RGB)
        ;; canvas (proxy [JLabel] []
        ;;          (paint [g] (.drawImage g image 0 0 this)))
        ]

    (paint-canvas size (.createGraphics image) caches assignment)
    
    ;; (doto (JFrame.)
    ;;   (.add canvas)
    ;;   (.setSize (Dimension. (+ 10 size) (+ 10 size)))
    ;;   (.show))
    image))

(defn animate-load
  []
  (let [mapped-items (map vector
                          core/items
                          (map core/map-to-unit-circle core/items))
        mapped-caches (map vector
                           core/caches
                           (map core/map-cache-to-unit-circle core/caches))
        simulated (core/simulation)

        load-pics 
        (map
         (fn [xs]
           (reduce
            (fn [acc [c is]]
              (merge-with +' acc {c (count is)}))
            {}
            xs))
         (reductions
          (fn [acc x]
            (let [cache-items (reduce
                               (fn [acc [c is]]
                                 (merge-with clojure.set/union acc {c (set is)}))
                               {}
                               (map vector x (map vector core/items)))]
              (merge-with clojure.set/union acc cache-items)))
          {}
          simulated))

        encoder (new GifEncoder)]
    ;;(draw 800 mapped-caches load-pics)
    (.start encoder "load.gif")
    (.setRepeat encoder 0)
    (doseq [assignments (rest load-pics)]
      (println assignments)
      (let [img (draw 500 mapped-caches assignments)]
        (.addFrame encoder img)))
    (.finish encoder)))
