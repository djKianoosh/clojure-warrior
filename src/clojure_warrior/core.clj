(ns clojure-warrior.core)

(def player-state (ref {:direction :east, :position [0,0], :level 1 } ))

(def directions #{:north :south :east :west}) ; not necessary

; info about the board
(def boards [
    [["@", " ", " ", ">"]] ; level 1
    []                     ; level 2
  ])


(defn get-board []
  (nth boards (dec (:level @player-state))))

; actions for the level
(defn next-position [player-position player-direction]
  (let [x (first player-position) y (second player-position)]
    (cond
      (= player-direction :east)
        [(inc x),y]
      (= player-direction :west)
        [(dec x),y]
      (= player-direction :north)
        [x,(inc y)]
      (= player-direction :south)
        [x,(dec y)])))

(defn get-board-position [x y]
  (let [board (get-board)]
    (nth (nth board y) x)))

(defn current-player-position []
  (let [player-position (:position @player-state) x (first player-position) y (second player-position)]
    (get-board-position x y)))

(defn did-you-win? []
  ; if current player position is on a ">"
  (= ">" (current-player-position)))

(defn walk []
  (let [player-position (:position @player-state) player-direction (:direction @player-state)]
    (let [ next-position (next-position player-position player-direction) x (first next-position) y (second next-position) next-tile (get-board-position x y)]
      (do
        (println "next position is " next-tile)
        (if (or (= next-tile " ") (= next-tile ">"))
          (dosync
            (alter player-state assoc :position next-position))
          (println "You hit a wall dummy!"))))) )

(def what-i-can-do [[walk]])

(defn what-can-i-do? []
  (nth what-i-can-do (dec (:level @player-state))))

(defn run-game [function]
  (loop []
    (function)
    (if (did-you-win?)
      (println "you rock the party")
      (recur))))
