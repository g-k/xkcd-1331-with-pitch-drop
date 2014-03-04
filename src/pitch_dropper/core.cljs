(ns pitch-dropper.core
  (:require
   [dommy.utils :as utils]
   [dommy.core :as dommy]
   [cljs.core.async
    :as async
    :refer [<! >! chan put! timeout]])
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:use-macros [dommy.macros :only [node sel sel1]]))


(defn log
  ;; previously: (. js/console (log Hello world! (foo 1 2)))
  [& args]
  (. js/console (log (str args))))

(defn mean
  [& xs]
  (/ (apply + xs) (count xs)))

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
  [
   {:class-name "heartbeat" :text "heartbeat" :frequency 0.86}
   {:class-name "birth" :text "one birth" :frequency 0.24}
   {:class-name "death" :text "one death" :frequency 0.56}
   {:class-name "wikipedia" :text "someone edits wikipedia" :frequency 0.67}
   {:class-name "vibrator" :text "someone buys a vibrator" :frequency 2.99}

   {:class-name "car_china" :text "China builds a car" :frequency 1.89}
   {:class-name "car_japan" :text "someone in Japan builds a car" :frequency 4.01}
   {:class-name "car_germany" :text "someone in Germany builds a car" :frequency 5.80}
   {:class-name "car_us" :text "the US builds a car" :frequency 6.95}
   {:class-name "car_elsewhere" :text "someone else builds a car" :frequency 1.03}

   {:class-name "kiss" :text "a European Union resident has their first kiss" :frequency 5.53}
   {:class-name "fire_dept" :text "a US fire department puts out a fire" :frequency 23.00}
   {:class-name "holeinone" :text "someone hits a hole-in-one" :frequency 180.00}
   {:class-name "turnsignal1" :text "my turn signal blinks" :frequency 0.94}
   {:class-name "turnsignal2" :text "the turn signal of the car in front of me blinks" :frequency 0.90}

   {:class-name "earthquake1" :text "earthquake (magnitude 1)" :frequency 2.43}
   {:class-name "earthquake2" :text "earthquake (magnitude 2)" :frequency 24.26}
   {:class-name "earthquake3" :text "earthquake (magnitude 3)" :frequency 242.60}
   {:class-name "earthquake4" :text "earthquake (magnitude 4)" :frequency 2426.00}
   {:class-name "parliament_toilet" :text "a member of the UK parliament flushes a toilet" :frequency 10.06}

   {:class-name "flight" :text "an airline takes off" :frequency 0.93}
   {:class-name "book_mockingbird" :text "someone buys To Kill A Mockingbird" :frequency 42.05}
   {:class-name "cat_mockingbird" :text "someone's pet cat kills a mockingbird" :frequency 1.82}
   {:class-name "phoenixshoes" :text "someone in phoenix buys new shoes" :frequency 1.08}
   {:class-name "phoenix" :text "someone in phoenix puts on a condom" :frequency 2.05}

   {:class-name "keys" :text "someone locks their keys in their car" :frequency 2.43}
   {:class-name "amelia" :text "a sagittarius named amelia drinks a soda" :frequency 7.79}
   {:class-name "dogbite" :text "a dog bites someone in the US" :frequency 7.01}
   {:class-name "bike" :text "someone steals a bicycle" :frequency 24.93}
   {:class-name "eagle" :text "a bald eagle catches a fish" :frequency 2.69}

   {:class-name "bottles" :text "50,000 plastic bottles are produced" :frequency 1.27}
   {:class-name "recycled" :text "50,000 plastic bottles are recycled" :frequency 4.64}
   {:class-name "meteor" :text "a bright meteor is visible somewhere" :frequency 1.15}
   {:class-name "oldfaithful" :text "old faithful erupts" :frequency 5640.00}
   {:class-name "shark" :text "a fishing boat catches a shark" :frequency 0.83}

   {:class-name "us_cancer" :text "someone in the US is diagnosed with cancer" :frequency 18.99}
   {:class-name "us_cancer_death" :text "someone in the US dies from cancer" :frequency 54.34}
   {:class-name "dog" :text "someone adopts a dog from a shelter" :frequency 15.60}
   {:class-name "cat" :text "someone adopts a cat from a shelter" :frequency 21.30}
   {:class-name "wedding" :text "someone gets married" :frequency 0.75}

   {:class-name "domain" :text "someone registers a domain" :frequency 0.64}
   {:class-name "house" :text "someone in the US buys a house" :frequency 6.22}
   {:class-name "tattoo" :text "someone in the US gets a tattoo" :frequency 2.06}
   {:class-name "pulsar" :text "the star PSR J1748-2446AD rotates 1,000 times" :frequency 1.40}
   {:class-name "facebook" :text "someone lies about their age to sign up for facebook" :frequency 4.32}

   {:class-name "iphone" :text "someone breaks an iphone screen" :frequency 0.93}
   {:class-name "littleleague" :text "a little league player strikes out" :frequency 1.23}
   {:class-name "ndsex" :text "someone has sex in ND" :frequency 1.38}
   {:class-name "bieber" :text "Justin Bieber gains a follower on Twitter" :frequency 4.73}
   {:class-name "denverpizza" :text "someone in denver orders pizza" :frequency 1.27}

   {:class-name "pitch_blend" :text "one pitch drops" :frequency AVERAGE-PITCH-DROP}
   ])

; (log AVERAGE-PITCH-DROP)
; (log "timeout called with" (* AVERAGE-PITCH-DROP 1000))

(defn make-item-div
  "Draw text"
  [{:keys [class-name text]}]
  (node [:div {:class (str class-name " frequency item")}
         text]))

(defn toggle
  "Toggles el dim class"
  [class-name message]
  (log "toggling!" class-name message (. js/window.performance now))
  (dommy/toggle-class! (sel1 (str "." class-name)) "dim"))

(defn blink-with-freq-using-chan
  [toggle-events item]
  (let [seconds-to-ms #(* 1000 %)
        frequency (:frequency item)
        el (make-item-div item)]

    (dommy/append! (sel1 ".frequency-container") el)

    ;; turn box on and off at frequency
    ;; push a true message every frequency seconds
    (go (while true
          (<! (timeout (seconds-to-ms frequency)))
          (>! toggle-events [(:class-name item) "dim"])))

    ;; working on fading properly
    ;; (go (while true
    ;;       (<! (timeout (+ 100 (seconds-to-ms frequency))))
    ;;       (>! toggle-events [(:class-name item) "undim"])))
    ))



;; (let [toggle-events (chan)
;;       blink-with-freq (partial blink-with-freq-using-chan toggle-events)]

;;   (go (while true
;;         (toggler (<! toggle-events))))

;;   (blink-with-freq (first frequent-items)))


;; (doall (map blink-with-freq frequent-items))

;; todo:

; make relative rate editable (time per time)
; fade out depending on freq
; fix cljs core.async timeout for pitch blend
; fix perf (44mb in 3 seconds!)
