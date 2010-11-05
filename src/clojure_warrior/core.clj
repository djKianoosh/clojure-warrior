(ns clojure-warrior.core)

(def player-state (ref {:direction :east, :position [0,0], :level 1 } ))

(def directions #{:north :east :south :west})

; info about the board
(def boards [
    [["@", " ", " ", ">"]
     [" ", " ", " ", " "]
     [" ", " ", " ", " "]] ; level 1
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
    (nth (nth board (- (dec (count board)) y)) x)))

(defn current-player-board-position []
  (let [player-position (:position @player-state) x (first player-position) y (second player-position)]
    (get-board-position x y)))

(defn did-you-win? []
  ; if current player position is on a ">"
  (= ">" (current-player-board-position)))

(defn- make-player-face [dir]
  (dosync (alter player-state assoc :direction dir)))

(defn turn-left []
  (let [player-direction (:direction @player-state)]
    (cond
      (= :north player-direction)
        (make-player-face :west)
      (= :south player-direction)
        (make-player-face :east)
      (= :east player-direction)
        (make-player-face :north)
      (= :west player-direction)
        (make-player-face :south))))

(defn turn-right []
  (let [player-direction (:direction @player-state)]
    (cond
      (= :north player-direction)
        (make-player-face :east)
      (= :south player-direction)
        (make-player-face :west)
      (= :east player-direction)
        (make-player-face :south)
      (= :west player-direction)
        (make-player-face :north))))

(defn walk []
  (let [player-position (:position @player-state) player-direction (:direction @player-state)]
    (let [ next-position (next-position player-position player-direction) x (first next-position) y (second next-position) next-tile (get-board-position x y)]
      (do
        ;(println "next position is " next-tile)
        (if (or (= next-tile " ") (= next-tile ">"))
          (dosync
            (alter player-state assoc :position next-position))
          (println "You hit a wall dummy!"))))) )

(def what-i-can-do [[walk,turn-left,turn-right]])

(defn what-can-i-do? []
  (nth what-i-can-do (dec (:level @player-state))))

(defn advance-player []
  (dosync
    (alter player-state assoc :level (inc (:level @player-state)) :position [0,0] :direction :east)))

(defn print-board []
  (let [board (get-board) height (count board) width (count (first board))]
    (println)
    (println "***** start-of-board *****")
      (doall
        (for [y (range (dec height) -1 -1)]
          (do
            (println)
            (doall
              (for [x (range width) ]
                (let [board-position (.trim (get-board-position x y))]
                  (cond
                    (= (:position @player-state) [x,y])
                      (print "' @ '")
                    (= "@" board-position)
                      (print "' _ '")
                    (= "" board-position)
                      (print "' _ '")
                    :else
                      (print "'"board-position"'"))))))))
    (println)
    (println "***** end-of-board *****")))

(defn run-game [function]
  (loop []
    (print-board)
    (function)
    (if (did-you-win?)
      (do
        (print-board)
        (println "You win this round!")
        (advance-player)
        (println @player-state)) ; todo move player to next level
      (recur))))
