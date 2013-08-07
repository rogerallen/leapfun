;; polling via timer seems to work okay
(ns leapfun.f003
  (:use [seesaw core table])
  (:import (com.leapmotion.leap
            Controller
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
           :rows [{:name "X" :value "n/a"}
                  {:name "Y" :value "n/a"}
                  {:name "Z" :value "n/a"}]]))

(defn update-table-xyz
  [[x y z]]
  ;;(println "update-table-xyz" x y z)
  (update-at! @the-table
              0 {:value (format "%5.3f" x)}
              1 {:value (format "%5.3f" y)}
              2 {:value (format "%5.3f" z)}))

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
    (if-not (empty? xyzs)
      (update-table-xyz (first xyzs))
      (update-table-xyz [Double/NaN Double/NaN Double/NaN]))))

(defn poll-controller
  []
  (let [c (Controller.)]
    (if (.isConnected c)
      (update-table c))))

;; ----------------------------------------------------------------------
(defn make-frame [name on-close]
  (frame :title name
         :width 200 :height 800
         :content (border-panel
                   :center (scrollable (make-table)))
         :on-close on-close))

(defn run [name & {:keys [on-close] :or {on-close :exit}}]
  (let [f  (show! (make-frame name on-close))
        t  (select f [:#table])
        ti (timer (fn [_] (poll-controller))
                  :delay 10
                  :start? true)
        ]
    (swap! the-table (fn [_] t))
    (listen f :window-closing (fn [_] (.stop ti)))
    nil))

;; ----------------------------------------------------------------------
(comment
  (run "title" :on-close :hide)
)
