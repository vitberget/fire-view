(ns fire-view.gamestuff
  (:use [clojure.pprint :only [pprint]])
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]))


;(defroutes
;  firestone
;  (POST "/createGame" []
;        (game-response (games/create-game! (create-a-game) "the-game-id")))
;  (POST "/endTurn" {body :body}
;        (game-response (games/end-turn! "the-game-id")))
;  (POST "/attack" {body :body}
;        (let [params (json/read-json (slurp body))
;              attacker-id (:attackerId params)
;              target-id (:targetId params)
;              ; TODO: Player-id should be taken from login
;              player-id (get-player-in-turn "the-game-id")]
;          (game-response (games/attack! "the-game-id" player-id attacker-id target-id))))
;  (POST "/playMinionCard" {body :body}
;        (let [params (json/read-json (slurp body))
;              card-id (:cardId params)
;              position (Integer. (:position params))
;              target-id (:targetId params)]
;          (game-response (games/play-minion-card! "the-game-id" card-id position target-id))))
;  (POST "/playCard" {body :body}
;        (let [params (json/read-json (slurp body))
;              card-id (:cardId params)
;              target-id (:targetId params)]
;          (game-response (games/play-spell-card! "the-game-id" card-id target-id))))
;  (POST "/useHeroPower" {body :body}
;        (let [params (json/read-json (slurp body))
;              player-id (:playerId params)
;              target-id (:targetId params)]
;          (game-response (games/use-hero-power! "the-game-id" player-id target-id))))
;  (POST "/undo" {body :body}
;        (let [params (json/read-json (slurp body))]
;          (game-response (games/undo! "the-game-id" 1)))))



(defn create-game []
  (println "create-game")
  (let [post (client/post "http://127.0.0.1:8001/createGame")
        body (:body post)
        result (json/read-str body :key-fn keyword)]
    (pprint result)
    result))

(defn end-turn []
  (println "end-turn")
  (let [post (client/post "http://127.0.0.1:8001/endTurn")
        body (:body post)
        result (json/read-str body :key-fn keyword)]
    (pprint result)
    result))

(defn undo []
  (println "undo")
  (let [post (client/post "http://127.0.0.1:8001/undo")
        body (:body post)
        result (json/read-str body :key-fn keyword)]
    (pprint result)
    result))

(defn play-card [card-id target-id]
  (println "play-card" card-id target-id)
  (let [data (if (nil? target-id) {:cardId card-id}
                                  {:cardId   card-id
                                   :targetId target-id})
        post (client/post "http://127.0.0.1:8001/playCard" {:body (json/write-str data)})
        body (:body post)
        result (json/read-str body :key-fn keyword)]
    (pprint result)
    result
    ))

(defn attack [attacker-id target-id]
  (println "attack" attacker-id target-id)
  (let [post (client/post "http://127.0.0.1:8001/attack" {:body (json/write-str {:attackerId attacker-id :targetId target-id})})
        body (:body post)
        result (json/read-str body :key-fn keyword)]
    (pprint result)
    result
    ))

(defn play-minion-card [card-id position target-id]
  (println "play-minion-card" card-id position target-id)
  (let [data (if (nil? target-id) {:cardId   card-id
                                   :position position}
                                  {:cardId   card-id
                                   :position position
                                   :targetId target-id})
        post (client/post "http://127.0.0.1:8001/playMinionCard" {:body (json/write-str data)})
        body (:body post)
        result (json/read-str body :key-fn keyword)]
    (pprint result)
    result
    ))

(def example-gamestate {:id                 "todo"
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
  )