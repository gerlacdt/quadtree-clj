(ns binarytree.core)

(defrecord TreeNode [val l r])

(defn make-tree [root]
  (TreeNode. root nil nil))

(defn tree-append [t v]
  (cond
    (nil? t) (TreeNode. v nil nil)
    (< v (:val t)) (TreeNode. (:val t) (tree-append (:l t) v) (:r t))
    :else (TreeNode. (:val t) (:l t) (tree-append (:r t) v))))

(defn traverse-in-order [t]
  (when t
    (concat (traverse-in-order (:l t)) [(:val t)] (traverse-in-order (:r t)))))

(def sample-tree (make-tree 1))
(def sample-tree (reduce tree-append sample-tree [3 5 2 4 6]))
(traverse-in-order sample-tree)
(traverse-in-order  (tree-append sample-tree 45))
