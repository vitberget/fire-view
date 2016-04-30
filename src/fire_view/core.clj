(ns fire-view.core
  (:use [clojure.pprint :only [pprint]])
  (:require [quil.core :as q :include-macros true]
            [fire-view.math :as m]
            [fire-view.test :as test]
            [fire-view.gamestuff :as game]))

(declare graphic-constants)

(def gamestate (atom game/example-gamestate))

(def graphic-state (atom {:translate-plane {:x 0 :y 0}
                          :eye             {:x 0 :y 0 :z 1850}
                          :fov             (/ m/pi 3)
                          :pressed         nil}))

(defn get-player-map []
  (->> (:players @gamestate)
       (filter (fn [player] (= (:id player) (:playerInTurn @gamestate))))
       (first)))

(defn get-enemy-map []
  (->> (:players @gamestate)
       (filter (fn [player] (not= (:id player) (:playerInTurn @gamestate))))
       (first)))

(defn
  ^{:doc "Only works correctly when flat"}
  mouse-to-zplane []
  (let [factor 1000
        half_width (/ (q/width) 2)
        half_height (/ (q/height) 2)
        aspect (- (q/screen-x factor 0) half_width)
        result {:x (* (/ (- (q/mouse-x) half_width) aspect) factor)
                :y (* (/ (- (q/mouse-y) half_height) aspect) factor)}]
    ;(println result)
    result))

(defn innercam []
  (let [eye (:eye @graphic-state)]
    (q/camera (:x eye) (:y eye) (:z eye)
              0 0 0
              0 1 0)))
(defn camera []
  (q/perspective (:fov @graphic-state)
                 (/ (q/width) (q/height))
                 60 5000)
  (innercam))

(defn
  ^{:doc "Only works correctly when flat"}
  mouse-to-zplane-notrans []
  (q/push-matrix)
  (innercam)
  (let [mouse (mouse-to-zplane)]
    (q/pop-matrix)
    mouse))

(defn get-entity-translation-x [pos size]
  (let [card-width (:card-width graphic-constants)
        card-spacing (:card-spacing graphic-constants)
        real-card-width (* card-width 2)
        hand-width (+ (* size real-card-width) (* card-spacing (- size 1)))
        hand-offset (- (/ hand-width 2.0) hand-width)
        card-offset (* pos (+ real-card-width card-spacing))]
    (+ hand-offset card-offset card-width)))

(defn inside-card? [mzpos idx hand-size]
  (m/inside-entity? (:x mzpos) (:y mzpos)
                    (get-entity-translation-x idx hand-size) (:hand-y graphic-constants)
                    (:card-width graphic-constants) (:card-height graphic-constants)))

(defn inside-endbutton? [mzpos]
  (m/inside-entity? (:x mzpos) (:y mzpos)
                    (:x (:end-button-translate graphic-constants))
                    (:y (:end-button-translate graphic-constants))
                    (:w (:end-button-size graphic-constants))
                    (:h (:end-button-size graphic-constants))))

(defn inside-friendly? [mzpos idx minion-count]
  (m/inside-entity? (:x mzpos) (:y mzpos)
                    (get-entity-translation-x idx minion-count) (:friendly-y graphic-constants)
                    (+ (:card-spacing graphic-constants) (:card-width graphic-constants)) (:card-height graphic-constants)))

(defn inside-enemy? [mzpos idx minion-count]
  (m/inside-entity? (:x mzpos) (:y mzpos)
                    (get-entity-translation-x idx minion-count) (:enemy-y graphic-constants)
                    (+ (:card-spacing graphic-constants) (:card-width graphic-constants)) (:card-height graphic-constants)))

(defn get-minion-target [mzpos playermap testfunction]
  (let [minions (:activeMinions playermap)
        minioncount (count minions)
        target (->> (map vector minions (iterate inc 0))
                    (filter (fn [item] (testfunction mzpos (last item) minioncount)))
                    (first)
                    (first))]
    target))

(defn mouse-released []
  (let [mzpos (mouse-to-zplane-notrans)
        mzpos-trans {:x (- (:x mzpos) (:x (:translate-plane @graphic-state)))
                     :y (- (:y mzpos) (:y (:translate-plane @graphic-state)))}
        card (:card (:pressed @graphic-state))
        minion (:minion (:pressed @graphic-state))]
    (cond
      ;-----------------------------------------------------------------------------------------------------------------
      (:translating (:pressed @graphic-state))
      (swap! graphic-state assoc :translate-plane {:x (+ (:x (:translate-plane @graphic-state)) (- (:x mzpos) (:x (:org-mouse (:pressed @graphic-state)))))
                                                   :y (+ (:y (:translate-plane @graphic-state)) (- (:y mzpos) (:y (:org-mouse (:pressed @graphic-state)))))})
      ;-----------------------------------------------------------------------------------------------------------------
      (= "MINION" (:type card))
      (let [minion-count-plus (inc (count (:activeMinions (get-player-map))))
            minion-position (->> (range minion-count-plus)
                                 (filter (fn [pos] (inside-friendly? mzpos-trans pos minion-count-plus)))
                                 (first))]
        (when-not (nil? minion-position)
          (swap! gamestate (fn [_] (game/play-minion-card (:id card) minion-position nil)))))
      ;-----------------------------------------------------------------------------------------------------------------
      (and (= "SPELL" (:type card)) (:isTargeting card))
      (let [target (or
                     (get-minion-target mzpos-trans (get-player-map) inside-friendly?)
                     (get-minion-target mzpos-trans (get-enemy-map) inside-enemy?))]
        (if-not (nil? target)
          (swap! gamestate (fn [_] (game/play-card (:id card) (:id target))))))
      ;-----------------------------------------------------------------------------------------------------------------
      (:canAttack minion)
      (let [target (or
                     (get-minion-target mzpos-trans (get-player-map) inside-friendly?)
                     (get-minion-target mzpos-trans (get-enemy-map) inside-enemy?))]
        (if-not (nil? target)
          (swap! gamestate (fn [_] (game/attack (:id minion) (:id target)))))))
    (swap! graphic-state assoc :pressed nil)))

(defn mouse-pressed []
  (let [mzpos (mouse-to-zplane-notrans)
        mzpos-trans {:x (- (:x mzpos) (:x (:translate-plane @graphic-state)))
                     :y (- (:y mzpos) (:y (:translate-plane @graphic-state)))}]
    (cond
      ;-----------------------------------------------------------------------------------------------------------------
      (inside-endbutton? mzpos-trans)
      (swap! gamestate (fn [_] (game/end-turn)))
      ;-----------------------------------------------------------------------------------------------------------------
      (and (q/key-pressed?) (:control (q/key-modifiers)))
      (swap! graphic-state assoc :pressed {:translating     true
                                           :org-mouse       mzpos
                                           :org-mouse-trans mzpos-trans}))
    ;-----------------------------------------------------------------------------------------------------------------
    (when (nil? (:pressed @graphic-state))
      (let [player (get-player-map)
            hand (:hand player)
            handcount (count hand)]
        (doseq [[card idx] (map vector hand (iterate inc 0))]
          (when (inside-card? mzpos-trans idx handcount)
            (swap! graphic-state assoc :pressed {:card            card
                                                 :org-mouse       mzpos
                                                 :org-mouse-trans mzpos-trans})))))
    ;-----------------------------------------------------------------------------------------------------------------
    (when (nil? (:pressed @graphic-state))
      (let [player (get-player-map)
            minions (:activeMinions player)
            minioncount (count minions)]
        (doseq [[minion idx] (map vector minions (iterate inc 0))]
          (when (inside-friendly? mzpos-trans idx minioncount)
            (swap! graphic-state assoc :pressed {:minion          minion
                                                 :org-mouse       mzpos
                                                 :org-mouse-trans mzpos-trans})))))))

(defn setup []
  (q/frame-rate 60)
  (swap! gamestate (fn [_] (game/create-game)))
  )

(load "draw")

(q/defsketch firestone-view
             :size :fullscreen
             :size [640 400]
             :title "FireView"
             :features [:exit-on-close
                        :resizable]
             :setup setup
             :draw draw-scene
             :renderer :p3d
             :mouse-wheel (fn [rotation] (swap! graphic-state update-in [:eye :z] + (* 25 rotation)))
             :mouse-pressed mouse-pressed
             :mouse-released mouse-released)
