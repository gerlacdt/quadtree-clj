(ns quadtree.performance.core-test
  (:require [clojure.test :refer :all]
            [quadtree.core :refer :all]
            [quadtree.performance.core :refer :all])
  (:import [quadtree.core Point]))

(deftest performance-test
  (let [feature-collections (load-geojson-file "files/feature_collections.json")
        features (get-points feature-collections)
        q-world (init-world)
        root (time (bulk-insert q-world features))]
    (testing "quadtree all car2gos"
      (is (= 208 (count (query root {:nw {:x 9.0439484 :y 48.8042085}
                                     :se {:x 9.2748333 :y 48.7096035}}))))
      (is (= 363 (count (query root {:nw {:x 8.8342369 :y 48.857922}
                                     :se {:x 9.3879723 :y 48.6405699}})))))))
