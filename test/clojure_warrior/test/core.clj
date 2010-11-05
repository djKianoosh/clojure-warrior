(ns clojure-warrior.test.core
  (:use [clojure-warrior.core] :reload)
  (:use [clojure.test]))

(defn play []
  (walk))

(run-game play)
