(ns clojure-warrior.core)

(def player-state (ref {:direction :east, :position [0,0], :level 1 } ))

(def directions #{:north :east :south :west}) ; not used, just in case you ever get confused?

; info about the board
(def boards [
    [["@", " ", " ", ">"]
     [" ", " ", " ", " "]
     [" ", " ", " ", " "]] ; level 1
    []                     ; level 2
  ])

(defn- get-board []
  (nth boards (dec (:level @player-state))))

; actions for the level
(defn- next-position [player-position player-direction]
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

; return 'X' if the coords are outside the board...
(defn- get-board-position [x y]
  (let [board (get-board)]
    (if (>= y (count board))
      "X"
      (let [row (nth board (- (dec (count board)) y))]
        (if (>= x (count row))
          "X"
          (nth row x))))))

(defn- move-player-to-start []
  (let [board (get-board) height (count board) width (count (first board))]
    (doall
      (for [y (range (dec height) -1 -1)]
        (doall
          (for [x (range width) ]
            (if (= "@" (get-board-position x y))
              (do
                (dosync (alter player-state assoc :position [x,y]))
                (println "moved player to" [x,y] "for start of level" (:level @player-state))))))))))

(defn- board-at-current-player-position []
  (let [player-position (:position @player-state) x (first player-position) y (second player-position)]
    (get-board-position x y)))

(defn- did-you-win? []
  ; if current player position is on a ">"
  (= ">" (board-at-current-player-position)))

(defn- make-player-face [dir]
  (dosync (alter player-state assoc :direction dir)))

(defn- action-turn-left []
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

(defn- action-turn-right []
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

(defn- action-walk []
  (let [player-position (:position @player-state) player-direction (:direction @player-state)]
    (let [ next-position (next-position player-position player-direction) x (first next-position) y (second next-position) next-tile (get-board-position x y)]
      (do
        ;(println "next position is " next-tile)
        (if (or (= next-tile " ") (= next-tile ">"))
          (dosync
            (alter player-state assoc :position next-position))
          (println "You hit a wall dummy!"))))) )

(def turn-left (memoize action-turn-left))
(def turn-right (memoize action-turn-right))
(def walk (memoize action-walk))

; we need to limit to one action, but this limits to one call to each action per round for now
; of course, any decent clojure programmer can work around this, but it is the spirit that counts
; or some crap like that....
(defn- enable-actions []
  (def turn-left (memoize action-turn-left))
  (def turn-right (memoize action-turn-right))
  (def walk (memoize action-walk)))

(def what-i-can-do [[walk,turn-left,turn-right] ; level 1 actions
                    ])

(defn what-can-i-do? []
  (nth what-i-can-do (dec (:level @player-state))))

; move player to the next round
(defn- advance-player []
  (dosync
    (alter player-state assoc :level (inc (:level @player-state)) :position [0,0] :direction :east)))

(defn- print-board []
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
                    (= (:position @player-state) [x,y]) ; this is where the player is right now
                      (print "' @ '")
                    (= "@" board-position) ; this is where the player started, but might not be any longer
                      (print "' _ '")
                    (= "" board-position) ; this is an empty spot
                      (print "' _ '")
                    :else
                      (print "'"board-position"'"))))))))
    (println)
    (println "***** end-of-board *****")))

(defn run-game [function]
  (do
    (move-player-to-start)
    (loop []
      (enable-actions)
      (print-board)
      (function)
      (if (did-you-win?)
        (do
          (print-board)
          (println "You win this round!")
          (advance-player)
          (println @player-state)) ; todo move player to next level
        (recur)))))
