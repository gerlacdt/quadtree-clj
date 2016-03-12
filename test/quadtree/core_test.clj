(ns quadtree.core-test
  (:require [clojure.test :refer :all]
            [quadtree.core :refer :all])
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
