(ns quadtree.test.benchmark
  (:require [quadtree.core :as quadtree]
            [cheshire.core :as json]
            [criterium.core :as bench]
            [clojure.test :refer :all])
  (:import [quadtree.core Point]))

(defn load-geojson-file [filename]
  "Returns extracted points from given filename (usually geoJSON)"
  (json/parse-stream (clojure.java.io/reader filename) true))

(defn get-points [feature-collections]
  (mapcat (fn [feature-collection]
            (-> feature-collection :features))
          feature-collections))

(defn bulk-insert [tree features]
  (let [points (map (fn [feature]
                      (Point. (first (-> feature :geometry :coordinates))
                              (second (-> feature :geometry :coordinates))
                              feature
                              ))
                    features)]
    (quadtree/insert-points tree points)))

(def feature-collections (load-geojson-file "files/feature_collections.json"))
(def features (get-points feature-collections))
(def q-world (quadtree/init-world))

(deftest insert-all-car2gos
  (let [root (time (bulk-insert q-world features))]
    'done))
