(ns quadtree.utils
  (:require [quadtree.core :as quadtree]
            [cheshire.core :as json])
  (:import [quadtree.core Point]))

(defn time [fn msg]
  "Executes given partial function and returns duration in
  milliseconds, the result of function and the given message in a
  map."
  (let [start-time (System/nanoTime)
        result (fn)]
    {:duration (/ (- (System/nanoTime) start-time) 1e6)
     :result result
     :message msg}))

(defn extract-time [x]
  {:duration  (-> x :duration)
   :message (-> x :message)})

(defn summary-time [result-list]
  (let [average-duration (/ (reduce #(+ %1 (-> %2 :duration))
                                    0
                                    result-list)
                            (count result-list))]
    {:average-duration-ms average-duration
     :message (-> (first result-list) :message)
     :count (count result-list)}))

(defn load-geojson-file [filename]
  "Returns extracted points from given filename (usually geoJSON)"
  (json/parse-stream (clojure.java.io/reader filename) true))

(defn get-features [feature-collections]
  (mapcat (fn [feature-collection]
            (-> feature-collection :features))
          feature-collections))

(defn feature->point [feature]
  (Point. (first (-> feature :geometry :coordinates))
          (second (-> feature :geometry :coordinates))
          feature))

(defn bulk-insert-geojson [tree features]
  (let [points (map feature->point
                    features)]
    (quadtree/insert-points tree points)))

(defn rand-with-negative [n]
  (let [positive (rand-int 2)]
    (if (= 0 positive)
      (rand n)
      (- (rand n)))))

(defn random-point-world []
  (Point. (rand-with-negative 180) (rand-with-negative 90) {}))

(defn random-points []
  (repeatedly random-point-world))
