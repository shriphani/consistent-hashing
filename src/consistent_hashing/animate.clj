(ns consistent-hashing.animate
  (:require [consistent-hashing.core :as core])
  (:import [javax.swing JFrame JLabel]
           [java.awt.image BufferedImage]
           [java.awt Graphics Dimension Color]))

(defn draw-everything
  []
  ())

(defn paint-canvas [size graphics assignment]

  ;; draw the unit circle
  (.setColor graphics (Color. 255 0 0))
  (.drawOval graphics
             0
             0
             size
             size)

  )

(defn draw [size assignment]
  (let [image  (BufferedImage. size size BufferedImage/TYPE_INT_RGB)
        canvas (proxy [JLabel] []
                 (paint [g] (.drawImage g image 0 0 this)))]

    (paint-canvas size (.createGraphics image) assignment)

    (doto (JFrame.)
      (.add canvas)
      (.setSize (Dimension. size size))
      (.show))))

(defn animate-load
  []
  (let [mapped-items (map vector
                          core/items
                          (map core/map-to-unit-circle core/items))
        mapped-caches (map vector
                           core/caches
                           (map core/map-to-unit-circle core/caches))
        simulated (core/simulation)

        load-pics 
        (reductions
         (fn [acc x]
           (reduce
            (fn [acc [c is]]
              (merge
               acc
               {c (distinct is)}))
            {}
            (reduce
             (fn [acc [c is]]
               (merge-with concat acc {c is}))
             {}
             (map vector x (map vector core/items)))))
         {}
         simulated)]
    load-pics))
