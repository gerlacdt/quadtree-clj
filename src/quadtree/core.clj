(ns quadtree.core
  (:gen-class))

(def maxPoints 1)

(def boundary {:nw {:x 0 :y 10}
               :se {:x 10 :y 0}})

(defn q-contains? [boundary point]
  "Returns true if boundary contains given point."
  (cond (< (:x point) (-> boundary :nw :x)) false
        (> (:x point) (-> boundary :se :x)) false
        (> (:y point) (-> boundary :nw :y)) false
        (< (:y point) (-> boundary :se :y)) false
        :else true))

(defn q-intersects? [box1 box2]
  "Returns true if given bounding boxes intersect."
  (not (or  (> (-> box1 :nw :x) (-> box2 :se :x))
            (< (-> box1 :se :x) (-> box2 :nw :x))
            (< (-> box1 :nw :y) (-> box2 :se :y))
            (> (-> box1 :se :y) (-> box2 :nw :y)))))

(defrecord QuadTreeNode
    [boundary points
     northWest northEast
     southWest southEast])

(defn make-quadtree [boundary]
  (QuadTreeNode. boundary [] nil nil nil nil))

(defn get-child-boundaries [boundary]
  (let [mid-x (/ (+ (-> boundary :nw :x) (-> boundary :se :x)) 2)
        mid-y (/ (+ (-> boundary :nw :y) (-> boundary :se :y)) 2)]
    {:northWest {:nw {:x (-> boundary :nw :x)
                      :y (-> boundary :nw :y)}
                 :se {:x mid-x
                      :y mid-y}}
     :northEast {:nw {:x mid-x
                      :y (-> boundary :nw :y)}
                 :se {:x (-> boundary :se :x)
                      :y mid-y}}
     :southWest {:nw {:x (-> boundary :nw :x)
                      :y mid-y}
                 :se {:x mid-x
                      :y (-> boundary :se :y)}}
     :southEast {:nw {:x mid-x
                      :y mid-y}
                 :se {:x (-> boundary :se :x)
                      :y (-> boundary :se :y)}}}))

(defn insert-contained-points [points boundary]
  "Returns a seq which contains only the points which are in the
  area of the given boundary"
  (filter (fn [p]
            (q-contains? boundary p)) points))


;; TODO no duplicate insertion of points which are on border!
(defn subdivide [{:keys [:boundary :points] :as node}]
  "Divides given node into 4 subnodes and returns them."
  (let [ boundaries (get-child-boundaries boundary)
        northWest (QuadTreeNode. (-> boundaries :northWest)
                                 (insert-contained-points
                                  points
                                  (-> boundaries :northWest))
                                 nil nil nil nil)
        northEast (QuadTreeNode. (-> boundaries :northEast)
                                 (insert-contained-points
                                  points
                                  (-> boundaries :northEast))
                                 nil nil nil nil)
        southWest (QuadTreeNode. (-> boundaries :southWest)
                                 (insert-contained-points
                                  points
                                  (-> boundaries :southWest))
                                 nil nil nil nil)
        southEast (QuadTreeNode. (-> boundaries :southEast)
                                 (insert-contained-points
                                  points
                                  (-> boundaries :southEast))
                                 nil nil nil nil)]
    {:northWest northWest :northEast northEast
     :southWest southWest :southEast southEast}))


(defn insert [tree point]
  ;; ignore point which does not belong in this node
  (cond
    (not (q-contains? (-> tree :boundary) point)) tree
    (and (not (-> tree :northWest))
         (< (count (-> tree :points)) maxPoints))
    (QuadTreeNode. (-> tree :boundary) (conj (-> tree :points) point) nil nil nil nil)
    (not (nil? (-> tree :northWest)))
    (QuadTreeNode. (-> tree :boundary) []
                   (insert (-> tree :northWest) point)
                   (insert (-> tree :northEast) point)
                   (insert (-> tree :southWest) point)
                   (insert (-> tree :southEast) point))
    :else
    (let [child-nodes (subdivide tree)]
      (QuadTreeNode. (-> tree :boundary) []
                     (insert (-> child-nodes :northWest) point)
                     (insert (-> child-nodes :northEast) point)
                     (insert (-> child-nodes :southWest) point)
                     (insert (-> child-nodes :southEast) point)))))

(defn insert-points [tree points]
  (reduce insert tree points))

;; (defn -main
;;   "I don't do a whole lot ... yet."
;;   [& args]
;;   (println "Hello, World!"))
