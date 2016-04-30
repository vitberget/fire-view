(ns fire-view.test
  (:use [clojure.test :only (is run-tests function?)]))

(defmacro is= [actual expected]
  `(let [actual# ~actual
         expected# ~expected
         equal# (= actual# expected#)]
     (do
       (when-not equal#
         (println "Actual:\t\t" actual# "\nExpected:\t" expected#))
       (is (= actual# expected#)))))

(defn
  ^{:doc  "Gets the index of the given element of the collection."
    :test (fn []
            (is= (index-of ["a" "b" "c"] "b")
                 1)
            (is= (index-of ["a" "b" "c"] "z")
                 nil)
            (is= (index-of [] "b")
                 nil))}
  index-of [haystack needle]
  (first (keep-indexed #(when (= %2 needle) %1) haystack)))

(defn
  ^{:test (fn []
            (is (vector-contains? ["a" "b" "c"] "a")))}
  vector-contains? [haystack needle]
  (not (nil? (index-of haystack needle))))





