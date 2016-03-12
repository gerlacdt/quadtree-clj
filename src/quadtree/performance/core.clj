(ns quadtree.performance.core
  (:require [cheshire.core :refer :all]
            [quadtree.core :as quadtree])
  (:import [quadtree.core Point]))

;; read car2go Feature Collections

(defn load-geojson-file [filename]
  "Returns extracted points from given filename (usually geoJSON)"
  (parse-stream (clojure.java.io/reader filename) true))

;; pub all GeoJSON Points into on list
(defn get-points [feature-collections]
  (mapcat (fn [feature-collection]
            (-> feature-collection :features))
          feature-collections))

;; insert all points to quadtree
(defn bulk-insert [tree features]
  (let [points (map (fn [feature]
                      (Point. (first (-> feature :geometry :coordinates))
                              (second (-> feature :geometry :coordinates))
                              feature
                              ))
                    features)]
    (quadtree/insert-points tree points)))
