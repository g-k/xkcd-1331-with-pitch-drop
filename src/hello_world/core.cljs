(ns hello-world.core
  (:require
   [clojure.browser.repl :as repl]
   [dommy.utils :as utils]
   [dommy.core :as dommy]
   [cljs.core.async
    :as async
    :refer [<! >! chan put! timeout]])
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:use-macros [dommy.macros :only [node sel sel1]]))

; Use of "localhost" will only work for local development.
; Change the port to match the :repl-listen-port.
; (repl/connect "http://localhost:9000/repl")

; try to do:
; the heart liney thing: http://danielwalsh.tumblr.com/post/30576493146/creating-curves-with-straight-lines-above-is-a
; or choc: http://www.fullstack.io/choc/

(defn foo [a b]
  (+ a b))

; append a new canvas to the body
(let [canvas (node [:canvas {:height 300 :width 500}])]
      (dommy/append! (sel1 "body") canvas)
      (doto (.getContext canvas "2d")
        (.beginPath)
        (.moveTo (/ 300 2) 0)
        (.lineTo 0 (/ 500 2))
        (.closePath)
        (.stroke)
        )
      )

(. js/console (log "Hello world!" (foo 1 2)))
