(ns quadtree.core
  (:gen-class))

(def maxPoints 100)

(def boundary {:nw {:x 0 :y 10}
               :se {:x 10 :y 0}})

(def world-boundary {:nw {:x -180 :y 90}
                     :se {:x 180 :y -90}})

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

(defn leaf? [node]
  "Returns true if given node is a leaf node."
  (if (-> node :northWest)
    false
    true))

(defrecord Point
    [x y data])

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

(defn find-points-to-insert [points boundary]
  "Returns a map with :included and :rest points to insert.
The :included one will be inserted. This is needed for deduplicating
of fringe-points."
  (reduce (fn [acc p]
            (if (q-contains? boundary p)
              (update-in acc [:included] conj p)
              (update-in acc [:rest] conj p))) {:included [] :rest []} points))

(defn subdivide [{:keys [:boundary :points] :as node}]
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
                                 nil nil nil nil)
        northEast (QuadTreeNode. (-> boundaries :northEast)
                                 (-> points-northEast :included)
                                 nil nil nil nil)
        southWest (QuadTreeNode. (-> boundaries :southWest)
                                 (->  points-southWest :included)
                                 nil nil nil nil)
        southEast (QuadTreeNode. (-> boundaries :southEast)
                                 (-> points-southEast :included)
                                 nil nil nil nil)]
    {:northWest northWest :northEast northEast
     :southWest southWest :southEast southEast}))


(defn insert [tree point]
  (cond
    (not (q-contains? (-> tree :boundary) point)) tree
    (and (leaf? tree)
         (< (count (-> tree :points)) maxPoints))
    (QuadTreeNode. (-> tree :boundary) (conj (-> tree :points) point) nil nil nil nil)
    (not (leaf? tree))
    (QuadTreeNode. (-> tree :boundary) []
                   (insert (-> tree :northWest) point)
                   (insert (-> tree :northEast) point)
                   (insert (-> tree :southWest) point)
                   (insert (-> tree :southEast) point))
    :else
    (let [child-nodes (subdivide tree)]
      (cond (q-contains? (-> child-nodes :northWest :boundary) point)
            (QuadTreeNode. (-> tree :boundary) []
                           (insert (-> child-nodes :northWest) point)
                           (-> child-nodes :northEast)
                           (-> child-nodes :southWest)
                           (-> child-nodes :southEast))
            (q-contains? (-> child-nodes :northEast :boundary) point)
            (QuadTreeNode. (-> tree :boundary) []
                           (-> child-nodes :northWest)
                           (insert (-> child-nodes :northEast) point)
                           (-> child-nodes :southWest)
                           (-> child-nodes :southEast))
            (q-contains? (-> child-nodes :southWest :boundary) point)
            (QuadTreeNode. (-> tree :boundary) []
                           (-> child-nodes :northWest)
                           (-> child-nodes :northEast)
                           (insert (-> child-nodes :southWest) point)
                           (-> child-nodes :southEast))
            :else
            (QuadTreeNode. (-> tree :boundary) []
                           (-> child-nodes :northWest)
                           (-> child-nodes :northEast)
                           (-> child-nodes :southWest)
                           (insert (-> child-nodes :southEast) point))))))

(defn insert-points [tree points]
  "Convinient functions in order to insert multiple points at once."
  (reduce insert tree points))

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

(defn number-of-nodes [node]
  "Returns the number of all nodes in the given root node (tree)."
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

(defn init-world []
  (make-quadtree world-boundary))

;; stuttgart bounding box
;; nw: 9.0439483  48.8042085
;; se: 9.2748333  48.7096035
;;  --> 208

;; all car2gos in Stuttgart 363
;; nw: 8.8342369  48.857922
;; sw: 9.3879723  48.6405699

;; (defn -main
;;   "I don't do a whole lot ... yet."
;;   [& args]
;;   (println "Hello, World!"))
