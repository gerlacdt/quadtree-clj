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
    [boundary points northWest northEast southWest southEast])

(defn make-quadtree [boundary point]
  (QuadTreeNode. boundary [point] nil nil nil nil))

(defn subdivide [boundary]
  "Divides given node into 4 subnodes and returns them."
  (let [mid-x (/ (+ (-> boundary :nw :x) (-> boundary :se :x)) 2)
        mid-y (/ (+ (-> boundary :nw :y) (-> boundary :se :y)) 2)
        northWest (QuadTreeNode. {:nw {:x (-> boundary :nw :x)
                                       :y (-> boundary :nw :y)}
                                  :se {:x mid-x
                                       :y mid-y}}
                                 [] nil nil nil nil)
        northEast (QuadTreeNode. {:nw {:x mid-x
                                       :y (-> boundary :nw :y)}
                                  :se {:x (-> boundary :se :x)
                                       :y mid-y}}
                                 [] nil nil nil nil)
        southWest (QuadTreeNode. {:nw {:x (-> boundary :nw :x)
                                       :y mid-y}
                                  :se {:x mid-x
                                       :y (-> boundary :se :y)}}
                                 [] nil nil nil nil)
        southEast (QuadTreeNode. {:nw {:x mid-x
                                       :y mid-y}
                                  :se {:x (-> boundary :se :x)
                                       :y (-> boundary :se :y)}}
                                 [] nil nil nil nil)]
    {:northWest northWest :northEast northEast
     :southWest southWest :southEast southEast}))


(defn insert [tree point]
  ;; ignore point which does not belong in this node

  ;; if there is space in this node, add point here
  ;; return true

  ;; otherwise subdivide quadtree-node

  ;; add point to subnode --> return true

  ;; return false (should never happen)

  )

;; (defn -main
;;   "I don't do a whole lot ... yet."
;;   [& args]
;;   (println "Hello, World!"))
