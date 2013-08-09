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
           :rows [{:name "hnd0"     :id "n/a" :valid "n/a" :x "n/a" :y "n/a" :z "n/a"}
                  {:name "hnd0fgr0" :id "n/a" :valid "n/a" :x "n/a" :y "n/a" :z "n/a"}
                  {:name "hnd0fgr1" :id "n/a" :valid "n/a" :x "n/a" :y "n/a" :z "n/a"}
                  {:name "hnd0fgr2" :id "n/a" :valid "n/a" :x "n/a" :y "n/a" :z "n/a"}
                  {:name "hnd0fgr3" :id "n/a" :valid "n/a" :x "n/a" :y "n/a" :z "n/a"}
                  {:name "hnd0fgr4" :id "n/a" :valid "n/a" :x "n/a" :y "n/a" :z "n/a"}
                  {:name "hnd1"     :id "n/a" :valid "n/a" :x "n/a" :y "n/a" :z "n/a"}
                  {:name "hnd1fgr0" :id "n/a" :valid "n/a" :x "n/a" :y "n/a" :z "n/a"}
                  {:name "hnd1fgr1" :id "n/a" :valid "n/a" :x "n/a" :y "n/a" :z "n/a"}
                  {:name "hnd1fgr2" :id "n/a" :valid "n/a" :x "n/a" :y "n/a" :z "n/a"}
                  {:name "hnd1fgr3" :id "n/a" :valid "n/a" :x "n/a" :y "n/a" :z "n/a"}
                  {:name "hnd1fgr4" :id "n/a" :valid "n/a" :x "n/a" :y "n/a" :z "n/a"}
                  ]]))

(defn update-table-xyz
  [row [x y z]]
  ;;(println "update-table-xyz" x y z)
  (update-at! @the-table row {:x (format "%5.3f" x)
                              :y (format "%5.3f" y)
                              :z (format "%5.3f" z)}))

(defmacro map-javalist
  [list f]
  `(for [i# (range (.count ~list))]
     (. (.get ~list i#) ~f)))

(defn get-hands-xyz
  [hands interaction-box]
  (for [i (range (.count hands))]
    (let [pos (.normalizePoint interaction-box
                               (.palmPosition (.get hands i))
                               false)]
      (vector (.getX pos) (.getY pos) (.getZ pos)))))

(defn get-pointable-xyz
  [pointable interaction-box]
  (for [i (range (.count pointable))]
    (let [pos (.normalizePoint interaction-box
                               (.tipPosition (.get pointable i))
                               false)]
      (vector (.getX pos) (.getY pos) (.getZ pos)))))

(defn update-row-missing
  [ii]
  (update-at! @the-table ii {:id "None"})
  (update-at! @the-table ii {:valid "None"})
  (update-table-xyz ii [Double/NaN Double/NaN Double/NaN]))

(defn update-row
  [i ii ids vlds xyzs]
  (try (let [id (nth ids i)
             vld (nth vlds i)
             xyz (nth xyzs i)]
         (update-at! @the-table ii {:id (str id)})
         (update-at! @the-table ii {:valid (str vld)})
         (update-table-xyz ii xyz))
       (catch IndexOutOfBoundsException e
         (update-row-missing ii))))

(defn update-table
  [ctlr]
  (let [frame           (.frame ctlr)
        hands           (.hands frame)
        interaction-box (.interactionBox frame)
        hand-ids        (map-javalist hands id)
        hand-vlds       (map-javalist hands isValid)
        finger-lists    (map-javalist hands fingers)
        hand-xyzs       (get-hands-xyz hands interaction-box)]
    (dotimes [i 2]
      (let [ii (* 6 i)]
        (update-row i ii hand-ids hand-vlds hand-xyzs)
        (try
          (let [fingers     (nth finger-lists i)
                finger-ids  (map-javalist fingers id)
                finger-vlds (map-javalist fingers isValid)
                finger-xyzs (get-pointable-xyz fingers interaction-box)]
            (dotimes [j 5]
              (let [jj (+ ii j 1)]
                (update-row j jj finger-ids finger-vlds finger-xyzs))))
          (catch IndexOutOfBoundsException e
            (dotimes [j 5]
              (let [jj (+ ii j)]
                (update-row-missing jj)))))))))

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
