(ns leapfun.f002
  (:use [seesaw core table])
  (:import (com.leapmotion.leap Controller
                                Listener
                                Frame
                                Hand Finger Tool Pointable
                                Vector))
  (:gen-class))

;; ----------------------------------------------------------------------
(def the-table (atom nil))

(defn make-table []
  (table
   :id :table
   :model [:columns [{:key :name  :text "Name"}
                     {:key :value :text "Value"}]
           :rows [{:name "Left/X" :value "n/a"}
                  {:name "Left/Y" :value "n/a"}
                  {:name "Left/Z" :value "n/a"}]]))

(defn make-frame [name on-close]
  (frame :title name
         :width 200 :height 800
         :content (border-panel
                   :center (scrollable (make-table)))
         :on-close on-close))

;; ----------------------------------------------------------------------
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
      (println "onFrame" c)) ;; add code that updates the-table
    ))

(defn- get-controller-listener
  "return a vector holding the [controller listener]"
  []
  (let [c (Controller.)
        l (mk-listener)]
    (loop [connected? (.isConnected c) recur-count 0]
      (if connected?
        (do
          (.addListener c l)
          [c l])
        (if (< recur-count 20)
          (do
            (Thread/sleep 100)
            (recur (.isConnected c) (inc recur-count)))
          (do
            (println "ERROR: Unable to connect to Leap Motion Controller!")
            [nil nil]))))))

;; ----------------------------------------------------------------------
(defn run [name & {:keys [on-close] :or {on-close :exit}}]
  (let [f (show! (make-frame name on-close))
        t (select f [:#table])
        [c l] (get-controller-listener)]
    (swap! the-table (fn [x] t))
    nil))

;; ----------------------------------------------------------------------
(comment
  (run :on-close :hide)
  (update-at! @the-table 0 ["Roger" "50"]) ;; row number & full row
  (column-count @the-table) ; 2
  (row-count @the-table) ; 3
)
