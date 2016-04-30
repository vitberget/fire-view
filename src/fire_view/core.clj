(ns fire-view.core
  (:use [clojure.pprint :only [pprint]])
  (:require [quil.core :as q :include-macros true]
            [fire-view.math :as m]
            [fire-view.test :as test]
            [fire-view.gamestuff :as game]))

(defmacro with-shape [& body]
  `(do (q/begin-shape)
       (try ~@body
            (finally (q/end-shape)))))

(def gamestate (atom {:id                 "todo"
                      :playerInTurn       "p1"
                      :intermediateStates []
                      :players            [{:id            "p1"
                                            :deckSize      1
                                            :hero          {:id        "h1"
                                                            :name      "Jaina"
                                                            :health    30
                                                            :maxHealth 30
                                                            :mana      10
                                                            :maxMana   10
                                                            :armor     0
                                                            :heropower {:name             "Fireblast"
                                                                        :manaCost         2
                                                                        :originalManaCost 2
                                                                        :canUse           true
                                                                        :isTargeting      true
                                                                        :validTargetIds   ["h1"
                                                                                           "h2"
                                                                                           "m-i2"
                                                                                           "m-i3"]}}
                                            :activeMinions [
                                                            {:attack           1
                                                             :buffDescriptions []
                                                             :canAttack        true
                                                             :description      nil
                                                             :health           1
                                                             :id               "m-i2"
                                                             :manaCost         1
                                                             :maxHealth        1
                                                             :name             "Imp"
                                                             :originalAttack   1
                                                             :originalHealth   1
                                                             :owner            "p1"
                                                             :position         0
                                                             :race             "demon"
                                                             :class            nil
                                                             :rarity           "common"
                                                             :states           []
                                                             :type             "Minion"
                                                             :validAttackIds   #{"h2" "m-i3"}}
                                                            {:attack           1
                                                             :buffDescriptions []
                                                             :canAttack        true
                                                             :description      nil
                                                             :health           1
                                                             :id               "m-i2"
                                                             :manaCost         1
                                                             :maxHealth        1
                                                             :name             "Imp"
                                                             :originalAttack   1
                                                             :originalHealth   1
                                                             :owner            "p1"
                                                             :position         0
                                                             :race             "demon"
                                                             :class            nil
                                                             :rarity           "common"
                                                             :states           []
                                                             :type             "Minion"
                                                             :validAttackIds   #{"h2" "m-i3"}}
                                                            {:attack           1
                                                             :buffDescriptions []
                                                             :canAttack        true
                                                             :description      nil
                                                             :health           1
                                                             :id               "m-i2"
                                                             :manaCost         1
                                                             :maxHealth        1
                                                             :name             "Imp"
                                                             :originalAttack   1
                                                             :originalHealth   1
                                                             :owner            "p1"
                                                             :position         0
                                                             :race             "demon"
                                                             :class            nil
                                                             :rarity           "common"
                                                             :states           []
                                                             :type             "Minion"
                                                             :validAttackIds   #{"h2" "m-i3"}}
                                                            ]
                                            :hand          [
                                                            {:id               "c-i1"
                                                             :name             "Imp"
                                                             :state            "in-hand"
                                                             :description      ""
                                                             :index            0
                                                             :health           1
                                                             :attack           1
                                                             :originalHealth   1
                                                             :originalAttack   1
                                                             :manaCost         1
                                                             :originalManaCost 1
                                                             :combo            false
                                                             :type             "MINION"
                                                             :race             "demon"
                                                             :isTargeting      false
                                                             :validTargetIds   []
                                                             :playable         true
                                                             :rarity           "common"
                                                             :class            nil}
                                                            {:id               "c-f1"
                                                             :index            0
                                                             :name             "Fireball"
                                                             :description      "Deal 6 damage."
                                                             :health           nil
                                                             :attack           nil
                                                             :manaCost         4
                                                             :originalManaCost 4
                                                             :combo            false
                                                             :originalHealth   nil
                                                             :originalAttack   nil
                                                             :type             "SPELL"
                                                             :rarity           "common"
                                                             :class            "mage"
                                                             :state            "in-hand"
                                                             :race             nil
                                                             :playable         true
                                                             :isTargeting      true
                                                             :validTargetIds   ["m-i2" "m-i3" "h1" "h2"]}]}
                                           {:id            "p2"
                                            :deckSize      0
                                            :hero          {:id        "h2"
                                                            :name      "Jaina"
                                                            :health    30
                                                            :maxHealth 30
                                                            :mana      10
                                                            :maxMana   10
                                                            :armor     0
                                                            :heropower {:name             "Fireblast"
                                                                        :manaCost         2
                                                                        :originalManaCost 2
                                                                        :canUse           false
                                                                        :isTargeting      true
                                                                        :validTargetIds   []}}
                                            :activeMinions [
                                                            {:attack           1
                                                             :buffDescriptions []
                                                             :canAttack        false
                                                             :description      nil
                                                             :health           1
                                                             :id               "m-i3"
                                                             :manaCost         1
                                                             :maxHealth        1
                                                             :name             "Imp"
                                                             :originalAttack   1
                                                             :originalHealth   1
                                                             :owner            "p2"
                                                             :position         0
                                                             :race             "demon"
                                                             :class            nil
                                                             :rarity           "common"
                                                             :states           []
                                                             :type             "Minion"
                                                             :validAttackIds   #{}}
                                                            ]
                                            :hand          [{:id               "c-i2"
                                                             :index            0
                                                             :name             "Imp"
                                                             :description      ""
                                                             :health           1
                                                             :attack           1
                                                             :manaCost         1
                                                             :originalManaCost 1
                                                             :originalHealth   1
                                                             :originalAttack   1
                                                             :state            "in-hand"
                                                             :combo            false
                                                             :race             "demon"
                                                             :class            nil
                                                             :type             "MINION"
                                                             :rarity           "common"
                                                             :playable         false
                                                             :isTargeting      false
                                                             :validTargetIds   []}]}]}
                     ))

(def graphic-state (atom {:translate-plane    {:x 0 :y 0}
                          :eye                {:x 0 :y 0 :z 1500}
                          :fov                (/ m/pi 3)
                          :clicked-start-data {}}))

(def ^:const graphic-constants {:card-width           170
                                :card-height          256
                                :card-spacing         20

                                :hover-factor         1.03

                                :hand-y               600
                                :friendly-y           0
                                :enemy-y              -600

                                :end-button-translate {:x 1000 :y 0}
                                :end-button-size      {:w 100 :h 50}})
(def images (atom {}))

(defn get-image [imagepath]
  (let [img (get @images imagepath)]
    (if img img (let [img (q/load-image (clojure.string/replace imagepath #"\s|," ""))]
                  (swap! images assoc imagepath img)
                  img))))

(defn get-portrait [portaitname]
  (get-image (str "portrait/" portaitname ".png")))

(defn get-player-map [state player-id]
  (->> (:players state)
       (filter (fn [player] (= (:id player) player-id)))
       (first)))

(defn get-enemy-map [state player-id]
  (->> (:players state)
       (filter (fn [player] (not= (:id player) player-id)))
       (first)))

(defn
  ^{:doc "Only works correctly when flat"}
  mouse-to-zplane []
  (let [factor 1000
        half_width (/ (q/width) 2)
        half_height (/ (q/height) 2)
        aspect (- (q/screen-x factor 0) half_width)]
    {:x (* (/ (- (q/mouse-x) half_width) aspect) factor)
     :y (* (/ (- (q/mouse-y) half_height) aspect) factor)}))

(defn camera []
  (q/perspective (:fov @graphic-state)
                 (/ (q/width) (q/height))
                 60 5000)
  (let [tx (:x (:translate-plane @graphic-state))
        ty (:y (:translate-plane @graphic-state))
        eye (:eye @graphic-state)]
    (q/camera (+ (:x eye) tx) (+ (:y eye) ty) (:z eye)
              tx ty 0
              0 1 0)))

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

(defn mouse-released []
  (let [cardid (:id (:card (:clicked-start-data @graphic-state)))]
    (when cardid
      (let [minion-count-plus (inc (count (:activeMinions (get-player-map @gamestate (:playerInTurn @gamestate)))))
            mzpos (mouse-to-zplane)
            position (->> (range minion-count-plus)
                          (filter (fn [pos] (when (inside-friendly? mzpos pos minion-count-plus)
                                              pos)))
                          (first))]
        (when-not (nil? position)
          (swap! gamestate (fn [_] (game/play-minion-card cardid position nil)))))))
  (swap! graphic-state assoc :clicked-start-data {}))

(defn mouse-pressed []
  (let [mzpos (mouse-to-zplane)]
    (cond
      (inside-endbutton? mzpos) (swap! gamestate (fn [_] (game/end-turn)))
      (q/key-pressed?) (swap! graphic-state assoc :clicked-start-data {:key                true
                                                                       :org-mouse-position mzpos
                                                                       :org-translate      (:translate-plane @graphic-state)})
      :else (let [player (get-player-map @gamestate (:playerInTurn @gamestate))
                  hand (:hand player)]
              (doseq [[card idx] (map vector hand (iterate inc 0))]
                (when (inside-card? mzpos idx (count hand))
                  (swap! graphic-state assoc :clicked-start-data {:card               card
                                                                  :org-mouse-position mzpos})))))))



(defn draw-card [mzpos card pos hand-size]
  (let [width (:card-width graphic-constants)
        height (:card-height graphic-constants)
        clicked-card-id (:id (:card (:clicked-start-data @graphic-state)))
        clicked (= (:id card) clicked-card-id)
        ph (- height 170)
        pw (- width 65)
        pw2 (- pw 35)
        pw3 (- pw2 40)
        pt (- 5 height)
        pt2 (+ pt 30)
        pto1 55
        pto2 105
        pto3 45]
    (q/with-translation [(get-entity-translation-x pos hand-size) 0]

                        (q/no-stroke)
                        (q/no-fill)

                        (when clicked
                          (let [morgpos (:org-mouse-position (:clicked-start-data @graphic-state))]
                            (q/translate (- (:x mzpos) (:x morgpos))
                                         (- (:y mzpos) (:y morgpos))
                                         10)))
                        (when (or clicked (and
                                            (not clicked-card-id)
                                            (inside-card? mzpos pos hand-size)))
                          (q/scale (:hover-factor graphic-constants)))

                        ; Portrait
                        (with-shape []
                                    (q/texture (get-portrait (:name card)))
                                    (q/vertex (- pw) pt2 87 pto3)
                                    (q/vertex (- pw2) pt2 (+ 87 pto1) pto3)
                                    (q/vertex (- pw3) pt (+ 87 pto2) 0)
                                    (q/vertex pw3 pt (- 423 pto2) 0)
                                    (q/vertex pw2 pt2 (- 423 pto1) pto3)
                                    (q/vertex pw pt2 423 pto3)
                                    (q/vertex pw ph 423 512)
                                    (q/vertex (- pw) ph 87 512)
                                    (q/vertex (- pw) pt 87 0))

                        ;Frame
                        (with-shape []
                                    (q/texture (get-image "card/frame.png")) ; TODO freames
                                    (q/vertex (- width) (- height) 87 3)
                                    (q/vertex width (- height) 423 3)
                                    (q/vertex width height 423 510)
                                    (q/vertex (- width) height 87 510)
                                    (q/vertex (- width) (- height) 87 3))

                        ; Name
                        (q/fill 0 0 0)
                        (q/text-size 40)
                        (q/text-align :center)
                        (q/translate 0 38)
                        (q/rotate-z -0.1)
                        (q/text (:name card) 0 0))))

(defn draw-minion [minion targeted]
  (let [width (:card-width graphic-constants)
        height (:card-height graphic-constants)
        ph (- height 170)
        pw (- width 65)
        pw2 (- pw 35)
        pw3 (- pw2 40)
        pt (- 5 height)
        pt2 (+ pt 30)
        pto1 55
        pto2 105
        pto3 45]
    (q/no-stroke)
    (q/no-fill)

    ; Portrait
    (with-shape []
                (q/texture (get-portrait (:name minion)))
                (q/vertex (- pw) pt2 87 pto3)
                (q/vertex (- pw2) pt2 (+ 87 pto1) pto3)
                (q/vertex (- pw3) pt (+ 87 pto2) 0)
                (q/vertex pw3 pt (- 423 pto2) 0)
                (q/vertex pw2 pt2 (- 423 pto1) pto3)
                (q/vertex pw pt2 423 pto3)
                (q/vertex pw ph 423 512)
                (q/vertex (- pw) ph 87 512)
                (q/vertex (- pw) pt 87 0))

    ; Frame
    (with-shape []
                (q/texture (get-image "card/frame.png"))    ; TODO minion-lookie-lookie
                (q/vertex (- width) (- height) 87 3)
                (q/vertex width (- height) 423 3)
                (q/vertex width height 423 510)
                (q/vertex (- width) height 87 510)
                (q/vertex (- width) (- height) 87 3))

    ; Name
    (q/with-translation [0 38]
                        (q/fill 0 0 0)
                        (q/text-size 40)
                        (q/text-align :center)
                        (q/rotate-z -0.1)
                        (q/text (:name minion) 0 0))

    (when targeted
      (q/translate 0 0 2)
      (q/fill 255 0 0 128)
      (let [w2 (+ width 5)
            h2 (+ height 5)]
        (q/rect (- w2) (- h2) (* 2 w2) (* 2 h2) 30)))
    ))

(defn draw-friendly-minion [mzpos minion idx minion-count card-drop-pos]
  (let [translation (cond (nil? card-drop-pos) [(get-entity-translation-x idx minion-count) 0]
                          (>= idx card-drop-pos) [(get-entity-translation-x (inc idx) (inc minion-count)) 0]
                          :else [(get-entity-translation-x idx (inc minion-count)) 0])
        clicked-card (:card (:clicked-start-data @graphic-state))]
    (q/with-translation translation (draw-minion minion (and
                                                          (:isTargeting clicked-card)
                                                          (= "SPELL" (:type clicked-card))
                                                          (test/vector-contains? (:validTargetIds clicked-card) (:id minion))
                                                          (inside-friendly? mzpos idx minion-count))))))

(defn draw-enemy-minion [mzpos minion idx minion-count]
  (q/with-translation [(get-entity-translation-x idx minion-count) 0]
                      (let [clicked-card (:card (:clicked-start-data @graphic-state))]
                      (draw-minion minion (and
                                            (:isTargeting clicked-card)
                                            (= "SPELL" (:type clicked-card))
                                            (test/vector-contains? (:validTargetIds clicked-card) (:id minion))
                                            (inside-enemy? mzpos idx minion-count))))))

(defn draw-friendly-minions [mzpos minions]
  (q/with-translation [0 (:friendly-y graphic-constants)]
                      (let [minion-count (count minions)
                            minion-count-plus (inc minion-count)
                            card-pos (when (= "MINION" (:type (:card (:clicked-start-data @graphic-state))))
                                       (->> (range minion-count-plus)
                                            (filter (fn [pos] (when (inside-friendly? mzpos pos minion-count-plus)
                                                                pos)))
                                            (first)))]
                        (doseq [[minion idx] (map vector minions (iterate inc 0))]
                          (draw-friendly-minion mzpos minion idx minion-count card-pos))
                        (when card-pos
                          (let [width (:card-width graphic-constants)
                                height (:card-height graphic-constants)]
                            (q/translate (get-entity-translation-x card-pos minion-count-plus) 0)
                            (q/fill 120 255 120 122)
                            (q/rect (- width) (- height) (* 2 width) (* 2 height) 30))))))

(defn draw-enemy-minions [mzpos minions]
  (q/with-translation [0 (:enemy-y graphic-constants) -2]
                      (let [minion-count (count minions)]
                        (doseq [[minion idx] (map vector minions (iterate inc 0))]
                          (draw-enemy-minion mzpos minion idx minion-count)))))

(defn draw-hand [mzpos hand]
  (q/with-translation [0 (:hand-y graphic-constants) 1]
                      (let [hand-size (count hand)
                            clicked-id (:id (:card (:clicked-start-data @graphic-state)))]
                        (doseq [[card idx] (map vector hand (iterate inc 0))]
                          (when-not (= clicked-id (:id card)))
                          (draw-card mzpos card idx hand-size))
                        ; TODO Better rendering of card!!! So no fucking need to cheat
                        (doseq [[card idx] (map vector hand (iterate inc 0))]
                          (when (= clicked-id (:id card))
                            (draw-card mzpos card idx hand-size))))))


(defn draw-endbutton [mzpos]
  (q/with-translation [(:x (:end-button-translate graphic-constants))
                       (:y (:end-button-translate graphic-constants))]
                      (if (inside-endbutton? mzpos)
                        (q/fill 56 255 56)
                        (q/fill 0 128 0))
                      (let [w (:w (:end-button-size graphic-constants))
                            h (:h (:end-button-size graphic-constants))]
                        (q/rect (- w) (- h) (* w 2) (* h 2) 30))))

(defn draw-board [mzpos]
  (draw-endbutton mzpos))

(defn draw-scene []
  (camera)
  (q/background 70 128 185)
  (let [mzpos (mouse-to-zplane)
        player (get-player-map @gamestate (:playerInTurn @gamestate))]
    (draw-board mzpos)
    (draw-enemy-minions mzpos (:activeMinions (get-enemy-map @gamestate (:playerInTurn @gamestate))))
    (draw-friendly-minions mzpos (:activeMinions player))
    (draw-hand mzpos (:hand player))))

(defn setup []
  (q/frame-rate 60)
  (swap! gamestate (fn [_] (game/create-game))))

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
