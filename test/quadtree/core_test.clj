(ns quadtree.core-test
  (:require [clojure.test :refer :all]
            [quadtree.core :refer :all]
            [quadtree.utils :as qutils])
  (:import [quadtree.core Point]))

(deftest quadtree-test
  (testing "Quadtrees"
    (let [boundary {:nw {:x 0 :y 10} :se {:x 10 :y 0}}
          t (make-quadtree boundary 1)
          t4 (insert-points t [(Point. 1 1 {}) (Point. 1 7 {})
                               (Point. 7 1 {}) (Point. 7 7 {})])
          t2-border (insert-points t [(Point. 1 1 {}) (Point. 5 1 {})])]
      (testing "creation"
        (is (= 1 1))
        (is (= t (make-quadtree boundary 1)))
        (is (= 1 (number-of-nodes t)))
        (is (= 5 (number-of-nodes t4)))
        (is (= 9 (number-of-nodes t2-border)))
        (is (= 2 (count (all-values t2-border)))))
      (testing "querying"
        (is (= (Point. 1 1 {}) (first (query t4 {:nw {:x 0 :y 5} :se {:x 5 :y 0}}))))
        (is (= 4 (count (query t4 {:nw {:x 0 :y 10} :se {:x 10 :y 0}}))))
        (is (= (set (list (Point. 1 1 {}) (Point. 1 7 {}))) (set (query t4 {:nw {:x 0 :y 10}
                                                                :se {:x 5 :y 0}}))))))))



;; geojson test (read geojson, map to points, insert them into quadtree)

;; part of stuttgart bounding box
;; nw: 9.0439483  48.8042085
;; se: 9.2748333  48.7096035
;;  --> 208

;; all car2gos in Stuttgart
;; nw: 8.8342369  48.857922
;; sw: 9.3879723  48.6405699
;; --> 363

(deftest geojson-test
  (let [feature-collections (qutils/load-geojson-file "files/feature_collections.json")
        features (qutils/get-features feature-collections)
        q-world (init-world)
        root (qutils/bulk-insert-geojson q-world features)]
    (testing "quadtree find car2gos in stuttgart"
      (is (= 208 (count (query root {:nw {:x 9.0439484 :y 48.8042085}
                                     :se {:x 9.2748333 :y 48.7096035}}))))
      (is (= 363 (count (query root {:nw {:x 8.8342369 :y 48.857922}
                                     :se {:x 9.3879723 :y 48.6405699}})))))))
