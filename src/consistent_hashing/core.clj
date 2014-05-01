(ns consistent-hashing.core
  (:require [digest])
  (:import [java.nio ByteBuffer]))

(def items (range 1000))

(def caches (range 10))

(defn map-to-unit-circle
  [x]
  (let [arr (ByteBuffer/allocate 4)
        val (do (.putInt arr x)
                (->> arr
                     (.array)
                     digest/md5
                     (.getBytes)
                     bigint))]
    (double
     (/ (rem val 100000)
        100000))))

(defn assign-item
  [[item pt] caches-points]
  (let [dsts (sort-by
              second
              (map
               (fn [[c p]]
                 [c (if (< p pt)
                      (+ p (- 1 pt))
                      (- p pt))])
               caches-points))]
    (first
     (first dsts))))

(defn assign
  "Items: a set of items
   Caches: a set of caches"
  [items caches]
  (let [mapped-items  (into
                       {}
                       (map
                        vector
                        items
                        (map map-to-unit-circle items)))
        mapped-caches (sort-by
                       second
                       (map
                        vector
                        caches
                        (map map-to-unit-circle caches)))]
    (map
     (fn [x]
       (assign-item x mapped-caches))
     mapped-items)))

(defn random-take
  [coll n]
  (let [coll-set (set coll)
        item (rand-nth (into [] coll-set))]
    (if (zero? n)
      []
      (cons item
            (random-take (clojure.set/difference coll-set (set [item]))
                         (dec n))))))

(defn simulation
  []
  (let [num-assgns 100]
    (map
     (fn [i]
       (let [to-take (rand-nth (range 1 (count caches)))
             seen-caches (random-take caches to-take)]
         (assign items seen-caches)))
     (range num-assgns))))
