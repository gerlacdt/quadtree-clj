(ns quadtree.test.benchmark
  (:require [quadtree.core :as quadtree]
            [quadtree.utils :as qutils]
            [cheshire.core :as json]
            [clojure.test :refer :all]
            [clojure.pprint :refer :all])
  (:import [quadtree.core Point]))

;; prepare car2go geoJSON insertion in quadtree
(def feature-collections (qutils/load-geojson-file "files/feature_collections.json"))
(def features (qutils/get-features feature-collections))
(def q-world (quadtree/init-world))
(def stuttgart-boundary {:nw {:x 8.8342369 :y 48.857922}
                         :se {:x 9.3879723 :y 48.6405699}})

(defn insert-all-car2go-time []
  (let [root (qutils/time (partial qutils/bulk-insert-geojson q-world features)
                          "11,000 car2gos geoJSONs inserted")]
    root))

(defn insert-random-points-time [n]
  (let [points-n (take n (qutils/random-points))
        root-n (qutils/time (partial
                             quadtree/insert-points q-world
                             points-n)
                            (str n " inserted"))]
    root-n))

(defn query [root boundary msg]
  (let [result (qutils/time (partial quadtree/query root boundary) msg)]
    (qutils/extract-time result)))

(deftest car2gos-test
  (pprint (qutils/summary-time (take 2 (repeatedly
                                        #(qutils/extract-time (insert-all-car2go-time)))))))

(deftest random-points-test
  (pprint (qutils/summary-time (take 2
                                     (repeatedly
                                      #(qutils/extract-time
                                        (insert-random-points-time 100000)))))))

(deftest query-car2gos
  (let [world-car2gos (qutils/bulk-insert-geojson q-world features)]
    (pprint (qutils/summary-time
             (take 1000
                   (repeatedly #(query world-car2gos
                                       stuttgart-boundary
                                       "query stuttgart car2gos")))))))

(deftest query-random-points
  (let [root (-> (insert-random-points-time 100000) :result)]
    (pprint (qutils/summary-time
             (take 1000
                   (repeatedly #(query root
                                       {:nw {:x 0 :y 10} :se {:x 10 :y 0}}
                                       "query random points 100,000")))))))
