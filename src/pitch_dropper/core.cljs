(ns pitch-dropper.core
  (:require
   [dommy.utils :as utils]
   [dommy.core :as dommy]
   [cljs.core.async
    :as async
    :refer [<! >! chan close! timeout]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:use-macros [dommy.macros :only [node sel sel1]]))

(enable-console-print!)

(defn log [& args] (. js/console (log (str args))))
(defn mean [& xs] (/ (apply + xs) (count xs)))
(defn seconds-to-ms [seconds] (* 1000 seconds))

(defn approx-months-to-seconds
  "months to seconds assuming ~30 days per month"
  [months]
  (apply * [months
            30 ; ~days per month
            24 ; ~hours per day
            3600 ; ~seconds per hour
            ]))

(def AVERAGE-PITCH-DROP
  "mean in seconds from months data at: https://en.wikipedia.org/wiki/Pitch_drop_experiment#Timeline"
  (approx-months-to-seconds
   (mean (mean 96 107) 99 86 97 99 104 111 148)))

(def frequent-items
  ;; vectors of [class-name text blink-frequency-in-seconds]
  [["heartbeat" "heartbeat" 0.86]
   ["birth" "one birth" 0.24]
   ["death" "one death" 0.56]
   ["wikipedia" "someone edits wikipedia" 0.67]
   ["vibrator" "someone buys a vibrator" 2.99]

   ["car_china" "China builds a car" 1.89]
   ["car_japan" "someone in Japan builds a car" 4.01]
   ["car_germany" "someone in Germany builds a car" 5.80]
   ["car_us" "the US builds a car" 6.95]
   ["car_elsewhere" "someone else builds a car" 1.03]

   ["kiss" "a European Union resident has their first kiss" 5.53]
   ["fire_dept" "a US fire department puts out a fire" 23.00]
   ["holeinone" "someone hits a hole-in-one" 180.00]
   ["turnsignal1" "my turn signal blinks" 0.94]
   ["turnsignal2" "the turn signal of the car in front of me blinks" 0.90]

   ["earthquake1" "earthquake (magnitude 1)" 2.43]
   ["earthquake2" "earthquake (magnitude 2)" 24.26]
   ["earthquake3" "earthquake (magnitude 3)" 242.60]
   ["earthquake4" "earthquake (magnitude 4)" 2426.00]
   ["parliament_toilet" "a member of the UK parliament flushes a toilet" 10.06]

   ["flight" "an airline takes off" 0.93]
   ["book_mockingbird" "someone buys To Kill A Mockingbird" 42.05]
   ["cat_mockingbird" "someone's pet cat kills a mockingbird" 1.82]
   ["phoenixshoes" "someone in phoenix buys new shoes" 1.08]
   ["phoenix" "someone in phoenix puts on a condom" 2.05]

   ["keys" "someone locks their keys in their car" 2.43]
   ["amelia" "a sagittarius named amelia drinks a soda" 7.79]
   ["dogbite" "a dog bites someone in the US" 7.01]
   ["bike" "someone steals a bicycle" 24.93]
   ["eagle" "a bald eagle catches a fish" 2.69]

   ["bottles" "50,000 plastic bottles are produced" 1.27]
   ["recycled" "50,000 plastic bottles are recycled" 4.64]
   ["meteor" "a bright meteor is visible somewhere" 1.15]
   ["oldfaithful" "old faithful erupts" 5640.00]
   ["shark" "a fishing boat catches a shark" 0.83]

   ["us_cancer" "someone in the US is diagnosed with cancer" 18.99]
   ["us_cancer_death" "someone in the US dies from cancer" 54.34]
   ["dog" "someone adopts a dog from a shelter" 15.60]
   ["cat" "someone adopts a cat from a shelter" 21.30]
   ["wedding" "someone gets married" 0.75]

   ["domain" "someone registers a domain" 0.64]
   ["house" "someone in the US buys a house" 6.22]
   ["tattoo" "someone in the US gets a tattoo" 2.06]
   ["pulsar" "the star PSR J1748-2446AD rotates 1,000 times" 1.40]
   ["facebook" "someone lies about their age to sign up for facebook" 4.32]

   ["iphone" "someone breaks an iphone screen" 0.93]
   ["littleleague" "a little league player strikes out" 1.23]
   ["ndsex" "someone has sex in ND" 1.38]
   ["bieber" "Justin Bieber gains a follower on Twitter" 4.73]
   ["denverpizza" "someone in denver orders pizza" 1.27]

   ["pitch_blend" "pitch drop" AVERAGE-PITCH-DROP]])


;; build layout
(defn item-div
  "Draw text"
  [[class-name text blink-frequency-in-seconds]]
  (node [:div {:class (str class-name " dim frequency item")}
         text]))

(def items (doall (map item-div frequent-items)))

(apply dommy/append! (concat [(sel1 ".frequency-container")] items))


;; dim and undim elements
(defn toggle-style
  "Toggles style on element"
  [[class-name property value]]
  (dommy/set-style! (sel1 (str "." class-name)) property value))

(def events (chan))  ; a stream of [class-name action value] events
(def dimmed-opacity 0.4)
(def undimmed-opacity 1.0)

(def MAX_TIMEOUT (. js/Math pow 2 31))

(defn long-timeout
  "Call timeout with MAX_TIMEOUT to handle signed 32-bit int overflow in setTimeout. Note that this can decrease the accuracy by the resolution times the number of timeouts that it needs to be split into."
  [msecs]
  (let [done (chan)]
    (go
      (loop [ms msecs]
        (<! (timeout ms))
        (if (> ms MAX_TIMEOUT)
          (recur (- ms MAX_TIMEOUT))
          (close! done))))
    done))


(defn toggle-with-frequency
  [[class-name text frequency :as item]]
  (let [frequency-in-ms (seconds-to-ms frequency)
        undim-duration (min (* 0.2 frequency-in-ms) 1000)
        dim-duration (- frequency-in-ms undim-duration)
        long-duration (> (max undim-duration dim-duration) MAX_TIMEOUT)
        timeout-fn (if long-duration
                     long-timeout
                     timeout)
        ]
    ;; (println class-name undim-duration dim-duration long-duration)

    (go (loop []
          ;; blocks until nil emitted at timeout

          (>! events [class-name "opacity" dimmed-opacity])
          (<! (timeout-fn dim-duration))

          (>! events [class-name "opacity" undimmed-opacity])
          (<! (timeout-fn undim-duration))

          (recur)))))

(doall (map toggle-with-frequency frequent-items))

(go (while true
      (toggle-style (<! events))))

;; TODO:
; make relative rate editable (time per time) to see pitch drops
