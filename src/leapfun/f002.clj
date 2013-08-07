;; BROKEN CODE?!?!
;;
;; this code seems to have issues in the interactions between seesaw
;; threads and the listener threads.  lein run only runs for a few
;; seconds before we get a spurious onExit call or some other hang.
;;
;; I don't see anything in particular wrong with the code, though.
;; And stripping out the seesaw bits allows this to run for a long
;; time.
(ns leapfun.f002
  (:use [seesaw core table])
  (:import (com.leapmotion.leap
            Controller
            Listener
            Frame
            Hand Finger Tool Pointable
            Vector))
  (:gen-class))

;; ----------------------------------------------------------------------
(defn now
  "Returns the current time in ms"
  []
  (System/currentTimeMillis))

(def the-table (atom nil))
(def last-table-update (atom (now)))
(def the-xyzs (atom [Double/NaN Double/NaN Double/NaN]))

(defn make-table []
  (table
   :id :table
   :model [:columns [{:key :name  :text "Name"}
                     {:key :value :text "Value"}]
           :rows [{:name "X" :value "n/a"}
                  {:name "Y" :value "n/a"}
                  {:name "Z" :value "n/a"}]]))

(defn update-table-xyz
  [xyz]
  (println "update-table-xyz" xyz)
  (swap! the-xyzs (fn [x] xyz)))

(comment
  (when (> (now) (+ @last-table-update 1000)) ;; update 5hz
    (println "update-table-xyz" x y z)
    (swap! last-table-update (fn [x] (now)))
    (update-at! @the-table
                0 ["X" (format "%5.2f" x)]
                1 ["Y" (format "%5.2f" y)]
                2 ["Z" (format "%5.2f" z)]))
)

(defn get-hands-xyz
  [hands interaction-box]
  (for [i (range (.count hands))]
    (let [pos (->> (.get hands i)
                   (.palmPosition)
                   (.normalizePoint interaction-box))]
      (vector (.getX pos) (.getY pos) (.getZ pos)))))

(defn update-table
  [ctlr]
  (let [frame           (.frame ctlr)
        hands           (.hands frame)
        interaction-box (.interactionBox frame)
        xyzs            (get-hands-xyz hands interaction-box)]
    (println "update-table" xyzs)
    (if-not (empty? xyzs)
      (update-table-xyz (first xyzs))
      (update-table-xyz [Double/NaN Double/NaN Double/NaN]))))

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
      (update-table c)) ;; add code that updates the-table
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
(defn make-frame [name on-close]
  (frame :title name
         :width 200 :height 800
         :content (border-panel
                   :center (scrollable (make-table)))
         :on-close on-close))

;; FIXME remove-listener on exit
(defn run [name & {:keys [on-close] :or {on-close :exit}}]
  (let [f (show! (make-frame name on-close))
        t (select f [:#table])
        [c l] (get-controller-listener)
        ]
    (swap! the-table (fn [x] t))
    nil))

;; ----------------------------------------------------------------------
(comment
  (run "title" :on-close :hide)
  (update-at! @the-table 0 ["Roger" "51"]) ;; row number & full row
  (column-count @the-table) ; 2
  (row-count @the-table) ; 3
  (update-at! @the-table
              0 ["X" "hi"]
              1 ["Y" "there"]
              2 ["Z" "yall"])
)
