(defproject hello-world "0.1.0-SNAPSHOT"
  :description "xkcd 1331: Frequency with Pitch Drop CLJS Experiment"
  :url "https://github.com/g-k/xkcd-1331-with-pitch-drop"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2227"]
                 [org.clojure/core.async "0.1.303.0-886421-alpha"]
                 [prismatic/dommy "0.1.2"]]

  :plugins [[lein-cljsbuild "1.0.3"]]

  :source-paths ["src"]

  :cljsbuild {
              :builds [{:id "dev"
                        :source-paths ["src"]
                        :compiler {
                                   :output-to "pitch_dropper.js"
                                   :output-dir "out"
                                   :optimizations :none
                                   :source-map true}}
                       {:id "prod"
                        :source-paths ["src"]
                        :compiler {
                                   :output-to "pitch_dropper.min.js"
                                   :optimizations :advanced
                                   :pretty-print false
                                   }}
                       ]})
