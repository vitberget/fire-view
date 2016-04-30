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
  (let [post (client/post "http://127.0.0.1:8001/createGame")
        body (:body post)
        result (json/read-str body :key-fn keyword)]
    (println "create-game")
    (pprint result)
    result))

(defn end-turn []
  (let [post (client/post "http://127.0.0.1:8001/endTurn")
        body (:body post)
        result (json/read-str body :key-fn keyword)]
    (println "end-turn")
    (pprint result)
    result))

(defn play-minion-card [card-id position target-id]
  (let [data (if (nil? target-id) {:cardId   card-id
                                   :position position}
                                  {:cardId   card-id
                                   :position position
                                   :targetId target-id})
        post (client/post "http://127.0.0.1:8001/playMinionCard" {:body (json/write-str data)})
        body (:body post)
        result (json/read-str body :key-fn keyword)]
    (println "play-minion-card" card-id position target-id)
    (pprint result)
    result
    ))