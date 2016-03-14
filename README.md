# quadtree-clj

This is a simple quadtree implementation in clojure. The quadtree is immutable
and therefore thread-safe.


## Leiningen

Leiningen provides the default "test" task.

    $ lein test

Additionally there exist a "benchmark" task.

    $ lein benchmark

## Usage

```clojure
[quadtree-clj "0.0.1"]

;; in your ns statement
(ns your.ns
  (:require [quadtree.core :as qtree])
  (:import [quadtree.core Point]))
```


## Examples

### Creation / Insertion

```clojure
; create quadtree with boundary and max 100 points in a leaf node

(def boundary {:nw {:x 0 :y 10} :se {:x 10 :y 0}}) ; define a bounding box

(qtree/make-quadtree boundary 100)

;; create quadtree with boundary (default max points is 100)

(qtree/make-quadtree boundary)

;; create quadtree for earth (min-x: -180, min-y: -90, max-x 180, max-y: 90)

(def world (qtree/init-world))

;; insertion of points

(def tree (qtree/insert-points world [(Point. 1 1 {})
                                      (Point. 1 7 {})]))

;; A point is defined by the coordinates: x,y and the data.
;; In data you can put everyting you want. You can think of x,y as the
;; key and data as the value of the point.

;; (defrecord Point
;;      [x y data])
```

### Deletion of points via predicate function

```clojure
;; insert some points
(def tree (qtree/insert-points world [(Point. 1 1 {:key "foo"})
                                      (Point. 1 7 {:key "bar"})]))

;; deletes all points with point.data.key === "foo"
;; delete operation does not consider geo-spatial index, so it has
;; linear complexity O(n)
(qtree/delete tree (fn [point] (= (-> point :data :key) "foo")))
```
### Querying and other stuff

```clojure
;; get all points in boundary

(def bounding-box {:nw {:x 0 :y 5} :se {:x 5 :y 0}})
(qtree/query tree bounding-box)

;; get all points of the given quadtree (linear complexity O(n)!)

(qtree/all-values tree)

;; get number of nodes in quadtree (intermediate nodes + leafs)

(qtree/number-of-nodes tree)
```
## License

Copyright © 2016 Daniel Gerlach

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
