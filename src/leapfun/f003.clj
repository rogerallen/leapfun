;; polling via timer seems to work okay
(ns leapfun.f003
  (:use [seesaw core table dev])
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
                     {:key :id    :text "ID"}
                     {:key :valid :text "Valid"}
                     {:key :x     :text "X"}
                     {:key :y     :text "Y"}
                     {:key :z     :text "Z"}
]
           :rows [{:name "hand 0" :id "n/a" :valid "n/a" :x "n/a" :y "n/a" :z "n/a"}
                  {:name "hand 1" :id "n/a" :valid "n/a" :x "n/a" :y "n/a" :z "n/a"}
                  ]]))

(defn update-table-xyz
  [row [x y z]]
  ;;(println "update-table-xyz" x y z)
  (update-at! @the-table row {:x (format "%5.3f" x)
                              :y (format "%5.3f" y)
                              :z (format "%5.3f" z)}))

(defmacro get-hands-fn
  [hands f]
  `(for [i# (range (.count ~hands))]
     (. (.get ~hands i#) ~f)))

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
        ids             (get-hands-fn hands id)
        vlds            (get-hands-fn hands isValid)
        xyzs            (get-hands-xyz hands interaction-box)]
    (dotimes [i 2]
      (let [ii (* 1 i)]
        (try (let [id (nth ids i)
                   vld (nth vlds i)
                   xyz (nth xyzs i)]
               (update-at! @the-table ii {:id (str id)})
               (update-at! @the-table ii {:valid (str vld)})
               (update-table-xyz ii xyz))
             (catch IndexOutOfBoundsException e
               (do
                 (update-at! @the-table ii {:id "None"})
                 (update-at! @the-table ii {:valid "None"})
                 (update-table-xyz ii [Double/NaN Double/NaN Double/NaN]))))))))

(defn poll-controller
  []
  (let [c (Controller.)]
    (if (.isConnected c)
      (update-table c))))

;; ----------------------------------------------------------------------
(defn make-frame [name on-close]
  (frame :title name
         :width 400 :height 400
         :content (border-panel
                   :center (scrollable (make-table)))
         :on-close on-close))

(defn run [name & {:keys [on-close] :or {on-close :exit}}]
  (let [f  (show! (make-frame name on-close))
        t  (select f [:#table])
        ti (timer (fn [_] (poll-controller))
                  :delay 50
                  :start? true)
        ]
    (swap! the-table (fn [_] t))
    (listen f :window-closing (fn [_] (.stop ti)))
    nil))

;; ----------------------------------------------------------------------
(comment
  (debug!)
  (run "title" :on-close :hide)
)
