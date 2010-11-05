(ns clojure-warrior.test.core
  (:use [clojure-warrior.core] :reload)
  (:use [clojure.test]))

(defn play []
  ; cheating! you are supposed to stick to one move per round!
  (walk)
;  (print-board)
;  (turn-left)
;  (walk)
;  (print-board)
;  (turn-right)
;  (walk)
;  (print-board)
;  (turn-left)
;  (walk)
;  (print-board)
;  (turn-right)
;  (walk))
          )
;(print-board)

(run-game play)
