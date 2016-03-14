(ns quadtree.core
  (:gen-class))

;; data structures

(defrecord Point
    [x y data])

(defrecord QuadTreeNode
    [boundary points
     northWest northEast
     southWest southEast
     maxPoints])


;; private

(defn- q-contains? [boundary point]
  "Returns true if boundary contains given point."
  (cond (< (:x point) (-> boundary :nw :x)) false
        (> (:x point) (-> boundary :se :x)) false
        (> (:y point) (-> boundary :nw :y)) false
        (< (:y point) (-> boundary :se :y)) false
        :else true))

(defn- q-intersects? [box1 box2]
  "Returns true if given bounding boxes intersect."
  (not (or  (> (-> box1 :nw :x) (-> box2 :se :x))
            (< (-> box1 :se :x) (-> box2 :nw :x))
            (< (-> box1 :nw :y) (-> box2 :se :y))
            (> (-> box1 :se :y) (-> box2 :nw :y)))))

(defn- leaf? [node]
  "Returns true if given node is a leaf node."
  (if (-> node :northWest)
    false
    true))

(defn- get-child-boundaries [boundary]
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

(defn- find-points-to-insert [points boundary]
  "Returns a map with :included and :rest points to insert.
The :included one will be inserted. This is needed for deduplicating
of fringe-points."
  (reduce (fn [acc p]
            (if (q-contains? boundary p)
              (update-in acc [:included] conj p)
              (update-in acc [:rest] conj p))) {:included [] :rest []} points))

(defn- subdivide [{:keys [:boundary :points] :as node}]
  "Divides given node into 4 subnodes and returns them."
  (let [boundaries (get-child-boundaries boundary)
        points-northWest (find-points-to-insert
                          points (-> boundaries :northWest))
        points-northEast (find-points-to-insert
                          (-> points-northWest :rest) (-> boundaries :northEast))
        points-southWest (find-points-to-insert
                          (-> points-northEast :rest) (-> boundaries :southWest))
        points-southEast (find-points-to-insert
                          (-> points-southWest :rest) (-> boundaries :southEast))
        northWest (QuadTreeNode. (-> boundaries :northWest)
                                 (-> points-northWest :included)
                                 nil nil nil nil (-> node :maxPoints))
        northEast (QuadTreeNode. (-> boundaries :northEast)
                                 (-> points-northEast :included)
                                 nil nil nil nil (-> node :maxPoints))
        southWest (QuadTreeNode. (-> boundaries :southWest)
                                 (->  points-southWest :included)
                                 nil nil nil nil (-> node :maxPoints))
        southEast (QuadTreeNode. (-> boundaries :southEast)
                                 (-> points-southEast :included)
                                 nil nil nil nil (-> node :maxPoints))]
    {:northWest northWest :northEast northEast
     :southWest southWest :southEast southEast}))


;; public

;; creation

(defn make-quadtree
  "Creates a quadtree with the given boundary and set the maximum
  number of points in a leaf to the given value. If maxPoints not
  given use 100 as default."
  ([boundary]
   (QuadTreeNode. boundary [] nil nil nil nil 100))
  ([boundary maxPoints]
   (QuadTreeNode. boundary [] nil nil nil nil maxPoints)))


(def world-boundary {:nw {:x -180 :y 90}
                     :se {:x 180 :y -90}})

(defn init-world []
  "Convenient function to initialize a quadtree which spans the
  earth. NorthWest: {:x -180 :y 90} and SouthEast {:x 180 :y -90}"
  (make-quadtree world-boundary))

;; insertion

(defn insert [tree point]
  "Inserts the given point into given quadtree. Returns a newly
   quadtree and does not mutate the given tree."
  (cond
    (not (q-contains? (-> tree :boundary) point)) tree
    (and (leaf? tree)
         (< (count (-> tree :points)) (-> tree :maxPoints)))
    (QuadTreeNode. (-> tree :boundary) (conj (-> tree :points) point)
                   nil nil nil nil (-> tree :maxPoints))
    (not (leaf? tree))
    (QuadTreeNode. (-> tree :boundary) []
                   (insert (-> tree :northWest) point)
                   (insert (-> tree :northEast) point)
                   (insert (-> tree :southWest) point)
                   (insert (-> tree :southEast) point)
                   (-> tree :maxPoints))
    :else
    (let [child-nodes (subdivide tree)]
      (cond (q-contains? (-> child-nodes :northWest :boundary) point)
            (QuadTreeNode. (-> tree :boundary) []
                           (insert (-> child-nodes :northWest) point)
                           (-> child-nodes :northEast)
                           (-> child-nodes :southWest)
                           (-> child-nodes :southEast)
                           (-> tree :maxPoints))
            (q-contains? (-> child-nodes :northEast :boundary) point)
            (QuadTreeNode. (-> tree :boundary) []
                           (-> child-nodes :northWest)
                           (insert (-> child-nodes :northEast) point)
                           (-> child-nodes :southWest)
                           (-> child-nodes :southEast)
                           (-> tree :maxPoints))
            (q-contains? (-> child-nodes :southWest :boundary) point)
            (QuadTreeNode. (-> tree :boundary) []
                           (-> child-nodes :northWest)
                           (-> child-nodes :northEast)
                           (insert (-> child-nodes :southWest) point)
                           (-> child-nodes :southEast)
                           (-> tree :maxPoints))
            :else
            (QuadTreeNode. (-> tree :boundary) []
                           (-> child-nodes :northWest)
                           (-> child-nodes :northEast)
                           (-> child-nodes :southWest)
                           (insert (-> child-nodes :southEast) point)
                           (-> tree :maxPoints))))))

(defn insert-points [tree points]
  "Convenient function in order to insert multiple points at once."
  (reduce insert tree points))

;; querying

(defn query [node bounding-box]
  "Returns all values which are contained in the given bounding box."
  (cond (not (q-intersects? (-> node :boundary) bounding-box))
        []
        (not (leaf? node))
        (concat (query (-> node :northWest) bounding-box)
                (query (-> node :northEast) bounding-box)
                (query (-> node :southWest) bounding-box)
                (query (-> node :southEast) bounding-box))
        :else
        (filter (fn [point]
                  (q-contains? bounding-box point)) (-> node :points))))

(defn delete [node predicate-fn]
  "Returns a quadtree without points which match the given predicate
  function. Not considering geo-spatial index. Hence deletion has O(n)
  complexity."
  (cond (leaf? node)
        (QuadTreeNode. (-> node :boundary)
                       (remove predicate-fn (-> node :points))
                       nil nil nil nil (-> node :maxPoints))
        :else (QuadTreeNode. (-> node :boundary)
                             []
                             (delete (-> node :northWest) predicate-fn)
                             (delete (-> node :northEast) predicate-fn)
                             (delete (-> node :southWest) predicate-fn)
                             (delete (-> node :southEast) predicate-fn)
                             (-> node :maxPoints))))

;; other functions

(defn number-of-nodes [node]
  "Returns the number of all nodes in the given quadtree."
  (cond (leaf? node) 1
        :else (+ 1
                 (number-of-nodes (-> node :northWest))
                 (number-of-nodes (-> node :northEast))
                 (number-of-nodes (-> node :southWest))
                 (number-of-nodes (-> node :southEast)))))

(defn all-values [node]
  "Returns all values of the given quadtree node recusivly (include
  all child nodes)"
  (cond
    (leaf? node) (-> node :points)
    :else (concat (all-values (-> node :northWest))
                  (all-values (-> node :northEast))
                  (all-values (-> node :southWest))
                  (all-values (-> node :southEast)))))
