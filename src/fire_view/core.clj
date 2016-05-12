(ns fire-view.core
  (:use [clojure.pprint :only [pprint]])
  (:require [quil.core :as q :include-macros true]
            [fire-view.math :as m]
            [fire-view.test :as test]
            [fire-view.gamestuff :as game]))

(declare graphic-constants)

(def gamestate (atom game/example-gamestate))

(def graphic-state (atom {:translate-plane {:x 0 :y 0}
                          :eye             {:x 0 :y 0 :z 2280}
                          :fov             (/ m/pi 3)
                          :pressed         nil
                          :rotations       {}}))

(defn get-rotation [id]
  (let [rotation (get (:rotations @graphic-state) id)]
    (if rotation
      rotation
      (let [rotation (- (rand 0.1) 0.05)]
        (swap! graphic-state assoc-in [:rotations id] rotation)
        rotation))))

(defn get-player-map []
  (first
    (->> (:players @gamestate)
         (filter (fn [player] (= (:id player) (:playerInTurn @gamestate)))))))

(defn get-enemy-map []
  (first
    (->> (:players @gamestate)
         (filter (fn [player] (not= (:id player) (:playerInTurn @gamestate)))))))

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
  (let [width (:w (:card-dimesions graphic-constants))
        card-spacing (:card-spacing graphic-constants)
        real-card-width (* width 2)
        hand-width (+ (* size real-card-width) (* card-spacing (- size 1)))
        hand-offset (- (/ hand-width 2.0) hand-width)
        card-offset (* pos (+ real-card-width card-spacing))]
    (+ hand-offset card-offset width)))



(defn inside-end-button? [mzpos]
  (m/inside-entity? mzpos (:end-button-translate graphic-constants) (:button-size graphic-constants)))

(defn inside-undo-button? [mzpos]
  (m/inside-entity? mzpos (:undo-button-translate graphic-constants) (:button-size graphic-constants)))

(defn inside-newgame-button? [mzpos]
  (m/inside-entity? mzpos (:newgame-button-translate graphic-constants) (:button-size graphic-constants)))

(defn inside-card? [mzpos idx hand-size]
  (m/inside-entity? mzpos {:x (get-entity-translation-x idx hand-size) :y (:hand-y graphic-constants)} (:card-dimesions graphic-constants)))

(defn inside-friendly? [mzpos idx minion-count]
  (m/inside-entity? mzpos
                    {:x (get-entity-translation-x idx minion-count)
                     :y (:friendly-y graphic-constants)}
                    (:card-dimesions graphic-constants)))

(defn inside-enemy? [mzpos idx minion-count]
  (m/inside-entity? mzpos {:x (get-entity-translation-x idx minion-count) :y (:enemy-y graphic-constants)} (:card-dimension-with-spacing graphic-constants)))

(defn get-minion-target [mzpos playermap testfunction]
  (let [minions (:activeMinions playermap)
        minioncount (count minions)]
    (first (first
             (->> (map vector minions (iterate inc 0))
                  (filter (fn [item] (testfunction mzpos (last item) minioncount))))))))

(defn mouse-released []
  (let [mzpos (mouse-to-zplane-notrans)
        mzpos-trans (m/coord- mzpos (:translate-plane @graphic-state))
        card (:card (:pressed @graphic-state))
        minion (:minion (:pressed @graphic-state))]
    (cond
      ;-TRANSLATING BOARD-----------------------------------------------------------------------------------------------
      (:translating (:pressed @graphic-state))
      (swap! graphic-state assoc :translate-plane (m/coord+ (:translate-plane @graphic-state) (m/coord- mzpos (:org-mouse (:pressed @graphic-state)))))
      ;-PLAY MINION-----------------------------------------------------------------------------------------------------
      ;TODO Minions with targeting
      (= "MINION" (:type card))
      (let [minion-count-plus (inc (count (:activeMinions (get-player-map))))
            minion-position (->> (range minion-count-plus)
                                 (filter (fn [pos] (inside-friendly? mzpos-trans pos minion-count-plus)))
                                 (first))]
        (when-not (nil? minion-position)
          (swap! gamestate (fn [_] (game/play-minion-card (:id card) minion-position nil)))))
      ;-PLAY SPELL------------------------------------------------------------------------------------------------------
      ;TODO Cards that do not target
      (and (= "SPELL" (:type card)) (:isTargeting card))
      (let [target (or
                     (get-minion-target mzpos-trans (get-player-map) inside-friendly?)
                     (get-minion-target mzpos-trans (get-enemy-map) inside-enemy?))]
        (if-not (nil? target)
          (swap! gamestate (fn [_] (game/play-card (:id card) (:id target))))))
      ;-MINION ATTACK---------------------------------------------------------------------------------------------------
      (:canAttack minion)
      (let [target (or
                     (get-minion-target mzpos-trans (get-player-map) inside-friendly?)
                     (get-minion-target mzpos-trans (get-enemy-map) inside-enemy?))]
        (if-not (or (nil? target) (= (:id minion) (:id target)))
          (swap! gamestate (fn [_] (game/attack (:id minion) (:id target)))))))
    (swap! graphic-state dissoc :pressed)))

(defn mouse-pressed []
  (let [mzpos (mouse-to-zplane-notrans)
        mzpos-trans (m/coord- mzpos (:translate-plane @graphic-state))]
    (cond
      ;-NEW TURN BUTTON-------------------------------------------------------------------------------------------------
      (inside-newgame-button? mzpos-trans)
      (swap! gamestate (fn [_] (game/create-game)))
      ;-END TURN BUTTON-------------------------------------------------------------------------------------------------
      (inside-end-button? mzpos-trans)
      (swap! gamestate (fn [_] (game/end-turn)))
      ;-UNDO BUTTON-----------------------------------------------------------------------------------------------------
      (inside-undo-button? mzpos-trans)
      (swap! gamestate (fn [_] (game/undo)))
      ;-TRANSLATING BOARD-----------------------------------------------------------------------------------------------
      (and (q/key-pressed?) (:control (q/key-modifiers)))
      (swap! graphic-state assoc :pressed {:translating     true
                                           :org-mouse       mzpos
                                           :org-mouse-trans mzpos-trans}))
    ;-CARD--------------------------------------------------------------------------------------------------------------
    (when (nil? (:pressed @graphic-state))
      (let [player (get-player-map)
            hand (:hand player)
            handcount (count hand)]
        (doseq [[card idx] (map vector hand (iterate inc 0))]
          (when (inside-card? mzpos-trans idx handcount)
            (swap! graphic-state assoc :pressed {:card            card
                                                 :org-mouse       mzpos
                                                 :org-mouse-trans mzpos-trans})))))
    ;-MINION------------------------------------------------------------------------------------------------------------
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
  (swap! gamestate (fn [_] (game/create-game))))

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
