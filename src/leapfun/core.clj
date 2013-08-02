(ns leapfun.core
  (:import (com.leapmotion.leap Controller
                                Listener
                                Frame
                                Hand Finger Tool Pointable
                                Vector))
  (:gen-class))

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
      (let [frame (.frame c)
            hands (.hands frame)
            pointables (.pointables frame)
            fingers (.fingers frame)
            tools (.tools frame)]
        (if (> (.count hands) 0)
          (let [left-hand (.leftmost hands)
                position (.palmPosition left-hand)]
            ;;(println "r=" (.sphereRadius left-hand)))))
            (println
             (format "[%6.1f, %6.1f, %6.1f]"
                     (.getX position) (.getY position) (.getZ position))))))
      )))

(defn- get-controller-listener []
  (let [c (Controller.)
        l (mk-listener)]
    (while (not (.isConnected c))
      (Thread/sleep 100))
    (.addListener c l)
    [c l]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  ;; work around dangerous default behaviour in Clojure
  (alter-var-root #'*read-eval* (constantly false))
  (println "Leap Fun!")
  (let [[c l] (get-controller-listener)]
    (println "Press Enter to quit")
    (read-line)
    (.removeListener c l)))
