(ns quadtree.core-test
  (:require [clojure.test :refer :all]
            [quadtree.core :refer :all]))



(deftest quadtree-insert-test
  (testing "Quadtrees"
    (let [boundary {:nw {:x 0 :y 10} :se {:x 10 :y 0}}
          t (make-quadtree boundary)
          t4 (insert-points t [{:x 1 :y 1} {:x 1 :y 7} {:x 7 :y 1} {:x 7 :y 7}])]
      (testing "creation"
        (is (= 1 1))
        (is (= t (make-quadtree boundary)))
        (is (= 1 (number-of-nodes t)))
        (is (= 5 (number-of-nodes t4))))
      (testing "querying"
        (is (= {:x 1 :y 1} (first (query t4 {:nw {:x 0 :y 5} :se {:x 5 :y 0}}))))
        (is (= 4 (count (query t4 {:nw {:x 0 :y 10} :se {:x 10 :y 0}}))))
        (is (= (set '({:x 1 :y 1} {:x 1 :y 7})) (set (query t4 {:nw {:x 0 :y 10}
                                                                :se {:x 5 :y 0}}))))))))
