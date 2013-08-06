(ns leapfun.f001
  (:require [overtone.live :as o])
  (:import (com.leapmotion.leap Controller
                                Listener
                                Frame
                                Hand Finger Tool Pointable
                                Vector))
  (:gen-class))

(def left-synth-id (atom -1))
(def right-synth-id (atom -1))
(o/definst synth [note 40 gate 1]
  (let [wave (o/sin-osc [(o/midicps note) (o/midicps note)])
        env  (o/env-gen (o/asr 0.1 1.0 0.1) :gate gate :action o/FREE)]
    (o/out 0 (* wave env))))

(defn synth-off [synth-id]
  (when (not= @synth-id -1)
    ;;(println "synth off" @synth-id)
    (o/ctl @synth-id :gate 0)
    (swap! synth-id (fn [x] -1))))

(defn hand-note
  [synth-id hand n]
  ;;(println "hand-note" synth-id hand n (.isValid hand) (= @synth-id -1))
  (if (.isValid hand)
    (if (= @synth-id -1)
      (do
        (swap! synth-id (fn [x] (synth n)))
        (println "new synth" n @synth-id))
      (o/ctl @synth-id :note n))
    (synth-off synth-id)))

(defn mk-listener []
  (proxy [Listener] []
    (onInit [c]
      (println "onInit" c))
    (onConnect [c]
      (println "onConnect" c))
    (onDisconnect [c]
      (println "onDisconnect" c))
    (onExit [c]
      (println "onExit" c))
    (onFrame [c]
      ;;(println "onFrame" c (.frame c))
      (let [frame           (.frame c)
            hands           (.hands frame)
            pointables      (.pointables frame)
            fingers         (.fingers frame)
            tools           (.tools frame)
            interaction-box (.interactionBox frame)]
        (if (> (.count hands) 0)
          (let [left-hand (.leftmost hands)
                right-hand (.rightmost hands)
                left-position  (.palmPosition left-hand)
                right-position (.palmPosition right-hand)
                left-position  (.normalizePoint interaction-box left-position)
                right-position  (.normalizePoint interaction-box right-position)
                [lx ly lz]   (vector
                              (.getX left-position) (.getY left-position) (.getZ left-position))
                [rx ry rz]   (vector
                              (.getX right-position) (.getY right-position) (.getZ right-position))
                ln         (nth (o/scale :A3 :pentatonic (range 5)) (int (* 5 ly)))
                rn         (nth (o/scale :A4 :pentatonic (range 8)) (int (* 8 ry)))
                ]
            ;;(println "r=" (.sphereRadius left-hand)))))
            ;;(println
            ;; (format "[%6.1f, %6.1f, %6.1f] %d %s" x y z n @synth-id))
            (hand-note left-synth-id left-hand ln)
            (if (> (.count hands) 1)
              (hand-note right-synth-id right-hand rn)
              (synth-off right-synth-id)))
          ;; else turn off synths
          (do
            (synth-off left-synth-id)
            (synth-off right-synth-id))
          ))
    )))

(defn- get-controller-listener []
  (let [c (Controller.)
        l (mk-listener)]
    (while (not (.isConnected c))
      (Thread/sleep 100))
    (.addListener c l)
    [c l]))

(defn run
  "main entry point."
  [& args]
  (let [[c l] (get-controller-listener)]
    (println "Press Enter to quit")
    (read-line)
    (.removeListener c l)
    (synth-off left-synth-id)
    (synth-off right-synth-id)))

(defn run1
  [seconds]
  (let [[c l] (get-controller-listener)]
    (Thread/sleep (* seconds 1000))
    (.removeListener c l)
    (synth-off left-synth-id)
    (synth-off right-synth-id)))

;;(run1 30)
;;(o/stop)
