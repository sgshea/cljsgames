(ns games.pong
  (:require [quil.core :as q :include-macros true]
            ))

(def WIDTH 450)
(def HEIGHT 200)

;; helper functions to build logic
(defn draw-rect
  "Draw a rectangle"
  [r]
  (q/rect
   (:x r)
   (:y r)
   (:w r)
   (:h r)))

;; Track when keys are pressed for movement
(def keys-pressed
  (atom {:w false
        :s false
        :up false
        :down false}))

;; define two paddles
(def l-paddle
  (atom {:x 10 :y (/ HEIGHT 3) :w 10 :h 70}))
(def r-paddle
  (atom {:x (- WIDTH 20) :y (/ HEIGHT 3) :w 10 :h 70}))

(defn draw-ball
  "Draw a ball"
  [e]
  (q/ellipse
    (:x e)
    (:y e)
    (:w e)
    (:h e)))

;; define ball with velocity
(def ball
  (atom {:x (/ WIDTH 2) :y (/ HEIGHT 2) :w 10 :h 10}))
(def ball-direction
  (atom [1.5 0]))
(defn next-ball
  "Calculate next ball position after step"
  [ball [ball-x ball-y]]
  (assoc ball :x (+ (:x ball) ball-x)
         :y (+ (:y ball) ball-y))
  )

;; functions handling pause and reset
(def paused?
  (atom false))

(defn reset-game
  "Resets game to default values"
  []
  (reset! ball {:x (/ WIDTH 2) :y (/ HEIGHT 2) :w 10 :h 10})
  (reset! ball-direction [1.5 0])
  (reset! l-paddle {:x 10 :y (/ HEIGHT 3) :w 10 :h 70})
  (reset! r-paddle {:x (- WIDTH 20) :y (/ HEIGHT 3) :w 10 :h 70})
  (reset! paused? false))

(defn game-menu
"To be displayed when game not playing"
  []
  (q/background 0 0 0)
  (q/stroke 255 255 255)
  (q/fill 255 255 255)
  (q/text-size 20)
  (q/text-align :center)
  (q/text "Press 'r' to resume, 'n' for new game" (/ WIDTH 2) (- HEIGHT 30))
  (q/text "Player 1 moves with 'w' and 's'" (/ WIDTH 2) (/ HEIGHT 3))
  (q/text "Player 2 moves with up and down arrow keys" (/ WIDTH 2) (/ HEIGHT 2))
  )

;; input
(defn key-press
  "Set paddle to be moving upon key press"
  []
  (cond
    (= (q/key-as-keyword) :w)
    (swap! keys-pressed assoc-in [:w] true)
    (= (q/key-as-keyword) :s)
    (swap! keys-pressed assoc-in [:s] true)
    (= (q/key-as-keyword) :ArrowUp)
    (swap! keys-pressed assoc-in [:up] true)
    (= (q/key-as-keyword) :ArrowDown)
    (swap! keys-pressed assoc-in [:down] true)
    ;; activate game menu
    (= (q/key-as-keyword) :r)
    (swap! paused? not)
    ;; reset game
    (= (q/key-as-keyword) :n)
    (reset-game)
    ))

(defn key-release
  "Set paddle to not be moving upon key release"
  []
  (cond
    (= (q/key-as-keyword) :w)
    (swap! keys-pressed assoc-in [:w] false)
    (= (q/key-as-keyword) :s)
    (swap! keys-pressed assoc-in [:s] false)
    (= (q/key-as-keyword) :ArrowUp)
    (swap! keys-pressed assoc-in [:up] false)
    (= (q/key-as-keyword) :ArrowDown)
    (swap! keys-pressed assoc-in [:down] false)
    ))

(defn intersects?
"Tests if two objects intersect"
  [a b]
  (if (and
        (<= (:x a) (+ (:x b) (:w b)))
        (>= (+ (:x a) (:w a)) (:x b))
        (<= (:y a) (+ (:y b) (:h b)))
        (>= (+ (:y a) (:h a)) (:y b)))
    true
    false))

(defn hit-factor [paddle ball]
  (-
   (/ (- (:y ball) (:y paddle)) (:h paddle))
   0.5))

;; quil functions

(defn setup-pong []
  (q/smooth)
  (q/no-stroke)
  (q/frame-rate 60))

;; draw
(defn draw-pong []
  (q/background 0x20)
  (q/fill 0xff)
  (draw-rect @l-paddle)
  (draw-rect @r-paddle)
  (draw-ball @ball)
  )

;; update
(defn update-pong []
  ;; move ball
  (swap! ball next-ball @ball-direction)
  ;; check movement
  (cond
    (true? (:w @keys-pressed))
      (swap! l-paddle update-in [:y] dec)
    (true? (:s @keys-pressed))
      (swap! l-paddle update-in [:y] inc)
    (true? (:up @keys-pressed))
      (swap! r-paddle update-in [:y] dec)
    (true? (:down @keys-pressed))
      (swap! r-paddle update-in [:y] inc))
  ;; invert direction if ball hits bounds
  (when (or (> (:y @ball) HEIGHT) (< (:y @ball) 0))
    (swap! ball-direction (fn [[x y]] [x (- y)])))
  ;; handle hitting paddles
  (when (intersects? @l-paddle @ball) 
    (swap! ball-direction (fn [[x _]] [(* x -1.1) (hit-factor @l-paddle @ball)])))
  (when (intersects? @r-paddle @ball) 
    (swap! ball-direction (fn [[x _]] [(* x -1.1) (hit-factor @r-paddle @ball)])))
  )

(defn pause-menu
  []
  (if @paused?
    (game-menu)
    (do
      (update-pong)
      (draw-pong))))

;; run
(q/defsketch pong
  :host "pong"
  :draw (fn [] (pause-menu))
  :size [WIDTH HEIGHT]
  :setup setup-pong
  :key-pressed key-press
  :key-released key-release)

;; entry point to display into html
(defn main []
  [:div {:id "pong"}])
