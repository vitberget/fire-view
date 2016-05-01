(ns fire-view.math
  (:use [fire-view.test :only [is=]]))

(defn coord+ [a b]
  {:x (+ (:x a) (:x b))
   :y (+ (:y a) (:y b))})

(defn coord- [a b]
  {:x (- (:x a) (:x b))
   :y (- (:y a) (:y b))})

(defn sqr [n] (* n n))

(defn sqrt [n] (Math/sqrt n))

(defn abs [n] (Math/abs n))

(defn tan [n] (Math/tan n))

(def pi (. Math PI))

(def vec+ (partial mapv +))

(def vec- (partial mapv -))

(def v= (comp (partial every? true?) (partial map ==)))

(defn vec-lenght-sqrt [u] (reduce + (map sqr u)))

(def vec-lenght (comp sqrt vec-lenght-sqrt))

(defn
  ^{:test (fn [] (is= (vec* [1 2 3] 2)
                      [2 4 6]))}
  vec* [u v]
  (cond
    (and (vector? u) (vector? v))                           ; both are vectors
    (mapv * u v)
    (vector? u)                                             ; u is a vector, v is a scalar
    (mapv (partial * v) u)
    (vector? v)                                             ; v is a vector, u is a scalar
    (mapv (partial * u) v)))


(defn vec-normalize-cheaty [u]
  (let [len (vec-lenght u)]
    [(/ (get u 0) len)
     (/ (get u 1) len)
     (/ (get u 2) len)]))

(defn vec-cross-product-cheaty [u v]
  (let [u1 (get u 0)
        u2 (get u 1)
        u3 (get u 2)
        v1 (get v 0)
        v2 (get v 1)
        v3 (get v 2)]
    [(- (* u2 v3) (* u3 v2))
     (- (* u3 v1) (* u1 v3))
     (- (* u1 v2) (* u2 v1))]))

(defn vec-dot [u v] (reduce + (map * u v)))



(defn ^{:doc "Return the point a segment and a plane intersects on, or nil"}
segment-plane-intersection [sp0 sp1 pv0 pn]
  (let [u (vec- sp1 sp0)
        w (vec- sp0 pv0)
        d (vec-dot pn u)
        n (- (vec-dot pn w))]
    (if (< (abs d) 0.0001)
      nil
      (let [si (/ n d)]
        (if (or (< si 0) (> si 1))
          nil
          (vec+ sp0 (vec* u si)))))))

(defn- ^{:test (fn []
                 (is= (segment-plane-intersection [10 0 0] [-10 0 0] [0 2 1] [1 0 0]) [0 0 0])
                 (is= (segment-plane-intersection [10 0 0] [-10 0 0] [2 2 1] [1 0 0]) [2 0 0])
                 (is= (segment-plane-intersection [20 20 10] [10 10 -10] [2 2 0] [0 0 1]) [15 15 0])
                 (is= (segment-plane-intersection [20 20 0] [10 10 0] [2 2 0] [1 0 0]) nil))}
test-tegment-plane-intersection [])

(defn inside-entity?
  ([z p d]
   (inside-entity? (:x z) (:y z)
                   (:x p) (:y p)
                   (:w d) (:h d)))
  ([zx zy x y w h]
   (and (< zx (+ x w))
        (> zx (- x w))
        (< zy (+ y h))
        (> zy (- y h)))))






