(ns games.core
    (:require
     [reagent.core :as r]
     [reagent.dom :as d]
     [games.pong :as pong]))

;; -------------------------
;; Views

(defn home-page []
  [:div
   [pong/main]])

;; -------------------------
;; Initialize app

(defn mount-root []
  (d/render [home-page] (.getElementById js/document "app")))

(def ^:export init! []
  (mount-root))
