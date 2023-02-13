(ns games.pong
  (:require [quil.core :as q :include-macros true]
            ))

;; helper functions to build logic
(defn draw-rect
  "Draw a rectangle"
  [r]
  (q/rect
   (:x r)
   (:y r)
   (:w r)
   (:h r)))

;; define two paddles
(def l-paddle
  (atom {:x 10 :y 65 :w 10 :h 70}))
(def r-paddle
  (atom {:x 430 :y 65 :w 10 :h 70}))

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
  (atom {:x 225 :y 100 :w 10 :h 10}))
(def ball-direction
  (atom [1 0]))
(defn next-ball
  "Calculate next ball position after step"
  [ball [ball-x ball-y]]
  (assoc ball :x (+ (:x ball) ball-x)
         :y (+ (:y ball) ball-y))
  )

(defn control-paddle
  "Change paddle height based on key pressed"
  []
  (cond
    (= (q/key-as-keyword) :w)
    (swap! l-paddle update-in [:y] dec)
    (= (q/key-as-keyword) :s)
    (swap! l-paddle update-in [:y] inc)
    (= (q/key-as-keyword) :ArrowUp)
    (swap! r-paddle update-in [:y] inc)
    (= (q/key-as-keyword) :ArrowDown)
    (swap! r-paddle update-in [:y] dec)
    ))

(defn hitfactor
  "Calculates direction of ball after hitting paddle"
  [ball paddle]
  (- (/ (- (:y ball) (:y paddle))
        (:h paddle))
     0.5))

(defn intersect
  "Calculates if the ball and a paddle are going to intersect
   and if so inverts ball and increases velocity"
  [ball left-paddle right-paddle]
  ;; left paddle
  (when (and
       (< (ball :x) (+ (left-paddle :x) (left-paddle :w) 10))
       (> (ball :y) (left-paddle :y))
       (< (ball :y) (+ (left-paddle :y) (left-paddle :h))))
    (swap! ball-direction (fn [[x _]] [[(- x) (hitfactor @l-paddle @ball)]])))
  (when (and
        (> (ball :x) (- (right-paddle :x) 10))
        (> (ball :y) (right-paddle :y))
        (< (ball :y) (+ (right-paddle :y) (right-paddle :h))))
    (swap! ball-direction (fn [[x _]] [[(- x) (hitfactor @r-paddle @ball)]]))))

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
  ;; invert direction if ball hits bounds
  (when (or (> (:y @ball) 200) (< (:y @ball) 0))
    (swap! ball-direction (fn [[x y]] [x (- y)])))
  ;; handle hitting paddles
  (intersect @ball @l-paddle @r-paddle)
  )

;; run
(q/defsketch pong
  :host "pong"
  :draw (fn [] (update-pong) (draw-pong))
  :size [450 200]
  :setup setup-pong
  :key-pressed control-paddle)

;; entry point to display into html
(defn main []
  [:div {:id "pong"}])
