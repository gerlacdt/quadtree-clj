(ns quadtree.test.benchmark
  (:require [quadtree.core :as quadtree]
            [quadtree.utils :as qutils]
            [cheshire.core :as json]
            [criterium.core :as bench]
            [clojure.test :refer :all]
            [clojure.pprint :refer :all])
  (:import [quadtree.core Point]))

;; prepare car2go geoJSON insertion in quadtree
(def feature-collections (qutils/load-geojson-file "files/feature_collections.json"))
(def features (qutils/get-features feature-collections))
(def q-world (quadtree/init-world))

(defn insert-all-car2gos []
  (let [root (qutils/time (partial qutils/bulk-insert-geojson q-world features)
                          "11,000 car2gos geoJSONs inserted")]
    (qutils/extract-time root)))

(defn insert-random-points [n]
  (let [points-n (take n (qutils/random-points))
        root-n (qutils/time (partial
                             quadtree/insert-points q-world
                             points-n)
                            (str n " inserted"))]
    (qutils/extract-time root-n)))

(deftest car2gos-test
  (pprint (qutils/summary-time (take 5 (repeatedly insert-all-car2gos)))))

(deftest random-points-test
  (pprint (qutils/summary-time (take 5
                                     (repeatedly #(insert-random-points 100000))))))
