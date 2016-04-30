(in-ns 'fire-view.core)

(defmacro with-shape [& body]
  `(do (q/begin-shape)
       (try ~@body
            (finally (q/end-shape)))))

(def ^:const graphic-constants {:card-width           170
                                :card-height          256
                                :card-spacing         20

                                :hover-factor         1.03

                                :hand-y               600
                                :friendly-y           0
                                :enemy-y              -600

                                :end-button-translate {:x 1600 :y 0}
                                :end-button-size      {:w 100 :h 50}})

(def images (atom {}))

(defn get-image [imagepath]
  (let [img (get @images imagepath)]
    (if img img (let [img (q/load-image (clojure.string/replace imagepath #"\s|," ""))]
                  (swap! images assoc imagepath img)
                  img))))

(defn get-portrait [portaitname]
  (get-image (str "portrait/" portaitname ".png")))

(defn draw-card [mzpos card pos hand-size]
  (let [width (:card-width graphic-constants)
        height (:card-height graphic-constants)
        clicked-card-id (:id (:card (:pressed @graphic-state)))
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
                          (let [morgpos (:org-mouse-trans (:pressed @graphic-state))]
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

(defn draw-minion [mzpos minion targeted]
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

    (when (= (:id minion) (:id (:minion (:pressed @graphic-state))))
      (let [morgpos (:org-mouse-trans (:pressed @graphic-state))]
        (q/translate (- (:x mzpos) (:x morgpos))
                     (- (:y mzpos) (:y morgpos))
                     10)))
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
      (if (and (:minion (:pressed @graphic-state))
               (not (:canAttack (:minion (:pressed @graphic-state)))))
        (q/fill 255 0 0 128)
        (q/fill 0 255 0 128))
      (let [w2 (+ width 5)
            h2 (+ height 5)]
        (q/rect (- w2) (- h2) (* 2 w2) (* 2 h2) 30)))
    ))

(defn mouse-attackable-minion? [mzpos idx minion-count test-function minion]
  (or
    (let [pressed (:card (:pressed @graphic-state))]
      (and
        pressed
        (:isTargeting pressed)
        (= "SPELL" (:type pressed))
        (test/vector-contains? (:validTargetIds pressed) (:id minion))
        (test-function mzpos idx minion-count)))
    (let [pressed (:minion (:pressed @graphic-state))]
      (and
        pressed
        (test/vector-contains? (:validAttackIds pressed) (:id minion))
        (test-function mzpos idx minion-count)))))

(defn draw-friendly-minion [mzpos minion idx minion-count card-drop-pos]
  (let [translation (cond (nil? card-drop-pos) [(get-entity-translation-x idx minion-count) 0]
                          (>= idx card-drop-pos) [(get-entity-translation-x (inc idx) (inc minion-count)) 0]
                          :else [(get-entity-translation-x idx (inc minion-count)) 0])]
    (q/with-translation translation
                        (draw-minion mzpos minion (mouse-attackable-minion? mzpos idx minion-count inside-friendly? minion)))))



(defn draw-enemy-minion [mzpos minion idx minion-count]
  (q/with-translation [(get-entity-translation-x idx minion-count) 0]
                      (draw-minion mzpos minion (mouse-attackable-minion? mzpos idx minion-count inside-enemy? minion))))

(defn draw-friendly-minions [mzpos minions]
  (q/with-translation [0 (:friendly-y graphic-constants)]
                      (let [minion-count (count minions)
                            minion-count-plus (inc minion-count)
                            card-pos (when (= "MINION" (:type (:card (:pressed @graphic-state))))
                                       (->> (range minion-count-plus)
                                            (filter (fn [pos] (inside-friendly? mzpos pos minion-count-plus)))
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
                            clicked-id (:id (:card (:pressed @graphic-state)))]
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
                        (q/rect (- w) (- h) (* w 2) (* h 2) 30))
                      (q/fill 0 0 0)
                      (q/text-size 70)
                      (q/translate 0 30)
                      (q/text-align :center)
                      (q/text "End" 0 0)))

(defn draw-board [mzpos]
  (draw-endbutton mzpos))

(defn draw-scene []
  (camera)
  (q/background 70 128 185)
  (let [mzpos (mouse-to-zplane)
        tx (:x (:translate-plane @graphic-state))
        ty (:y (:translate-plane @graphic-state))
        mzpos-trans {:x (- (:x mzpos) tx)
                     :y (- (:y mzpos) ty)}
        player (get-player-map)]
    (q/translate tx ty)
    (when
      (:translating (:pressed @graphic-state))
      (q/translate (- (:x mzpos) (:x (:org-mouse (:pressed @graphic-state))))
                   (- (:y mzpos) (:y (:org-mouse (:pressed @graphic-state))))))
    (draw-board mzpos-trans)
    (draw-enemy-minions mzpos-trans (:activeMinions (get-enemy-map)))
    (draw-friendly-minions mzpos-trans (:activeMinions player))
    (draw-hand mzpos-trans (:hand player))))