(ns cljsgames.core
    (:require
     [reagent.core :as r]
     [reagent.dom :as d]
     [cljsgames.pong :as pong]))

;; -------------------------
;; Views

(defn home-page []
  [:div 
   [pong/main]])

;; -------------------------
;; Initialize app

(defn mount-root []
  (d/render [home-page] (.getElementById js/document "app")))

(defn ^:export init! []
  (mount-root))
