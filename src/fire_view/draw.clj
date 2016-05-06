(in-ns 'fire-view.core)

(defmacro with-shape [& body]
  `(do (q/begin-shape)
       (try ~@body
            (finally (q/end-shape)))))

(defmacro xy [map]
  `(flatten (vals ~map)))

(def ^:const graphic-constants {:card-dimesions              {:w 170 :h 256}
                                :card-dimension-with-spacing {:w 190 :h 256}
                                :card-spacing                20

                                :hover-factor                1.03

                                :hand-y                      600
                                :friendly-y                  0
                                :enemy-y                     -600

                                :end-button-translate        {:x 1600 :y 0}
                                :undo-button-translate       {:x 1600 :y 125}
                                :newgame-button-translate    {:x 1600 :y -125}

                                :button-size                 {:w 200 :h 50}})

(def images (atom {}))

(defn get-image [imagepath]
  (let [img (get @images imagepath)]
    (if img img (let [img (q/load-image (clojure.string/replace imagepath #"\s|," ""))]
                  (swap! images assoc imagepath img)
                  img))))

(defn get-portrait [portaitname]
  (get-image (str "portrait/" portaitname ".png")))

(defn targetable-minion? [minion]
  (or
    (let [pressed (:card (:pressed @graphic-state))]
      (and
        (:isTargeting pressed)
        (= "SPELL" (:type pressed))
        (test/vector-contains? (:validTargetIds pressed) (:id minion))))
    ; Attackable by minion
    (let [pressed (:minion (:pressed @graphic-state))]
      (and
        pressed
        (test/vector-contains? (:validAttackIds pressed) (:id minion))))))

(defn targetted-minion? [mzpos idx minion-count test-function minion]
  (and
    (targetable-minion? minion)
    (test-function mzpos idx minion-count)))

(defn draw-card [mzpos card pos hand-size]
  (let [width (:w (:card-dimesions graphic-constants))
        height (:h (:card-dimesions graphic-constants))
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
                          (q/translate (xy (m/coord- mzpos (:org-mouse-trans (:pressed @graphic-state)))))
                          (q/translate 0 0 10))
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
  (let [width (:w (:card-dimesions graphic-constants))
        height (:h (:card-dimesions graphic-constants))
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

    (when
      (and
        (not targeted)
        (targetable-minion? minion))
      (q/with-translation [0 0 -2]
                          (q/fill 255 244 56)
                          (let [w2 (+ width 5)
                                h2 (+ height 5)]
                            (q/rect (- w2) (- h2) (* 2 w2) (* 2 h2) 30))))

    (when (= (:id minion) (:id (:minion (:pressed @graphic-state))))
      (q/translate (xy (m/coord- mzpos (:org-mouse-trans (:pressed @graphic-state)))))
      (q/translate 0 0 10))

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

(defn draw-friendly-minion [mzpos minion idx minion-count card-drop-pos]
  (let [translation (cond (nil? card-drop-pos) [(get-entity-translation-x idx minion-count) 0]
                          (>= idx card-drop-pos) [(get-entity-translation-x (inc idx) (inc minion-count)) 0]
                          :else [(get-entity-translation-x idx (inc minion-count)) 0])]
    (q/with-translation translation
                        (draw-minion mzpos minion (targetted-minion? mzpos idx minion-count inside-friendly? minion)))))



(defn draw-enemy-minion [mzpos minion idx minion-count]
  (q/with-translation [(get-entity-translation-x idx minion-count) 0]
                      (draw-minion mzpos minion (targetted-minion? mzpos idx minion-count inside-enemy? minion))))

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
                          (let [width (:w (:card-dimesions graphic-constants))
                                height (:h (:card-dimesions graphic-constants))]
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

(defn draw-button [text button-translation hover]
  (q/with-translation [(xy button-translation)]
                      (if hover
                        (q/fill 56 255 56)
                        (q/fill 0 128 0))
                      (let [w (:w (:button-size graphic-constants))
                            h (:h (:button-size graphic-constants))]
                        (q/rect (- w) (- h) (* w 2) (* h 2) 30))
                      (q/fill 0 0 0)
                      (q/text-size 70)
                      (q/translate 0 30)
                      (q/text-align :center)
                      (q/text text 0 0)))


(defn draw-board [mzpos]
  (draw-button "End turn" (:end-button-translate graphic-constants) (inside-end-button? mzpos))
  (draw-button "Undo move" (:undo-button-translate graphic-constants) (inside-undo-button? mzpos))
  (draw-button "New game" (:newgame-button-translate graphic-constants) (inside-newgame-button? mzpos)))

(defn draw-scene []
  (camera)
  (q/background 70 128 185)
  (let [mzpos (mouse-to-zplane)
        mzpos-trans (m/coord- mzpos (:translate-plane @graphic-state))
        player (get-player-map)]
    (q/translate (xy (:translate-plane @graphic-state)))
    (when
      (:translating (:pressed @graphic-state))
      (q/translate (xy (m/coord- mzpos (:org-mouse (:pressed @graphic-state))))))
    (draw-board mzpos-trans)
    (draw-enemy-minions mzpos-trans (:activeMinions (get-enemy-map)))
    (draw-friendly-minions mzpos-trans (:activeMinions player))
    (draw-hand mzpos-trans (:hand player))))