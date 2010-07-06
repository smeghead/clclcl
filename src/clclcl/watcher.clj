(ns clclcl.watcher
  (:gen-class)
  (:use clclcl.clipboard clclcl.database clclcl.tasktray)
  (:import (java.awt Toolkit)
     (java.awt.datatransfer  FlavorListener)))

(def *watcher-thread* (ref nil))

(defn watcher-loop []
  (let [s (clipboard-get)]
    (println (str "[" s "]"))
    (if s
      (db-insert s)))
  (tasktray-update-menu)
  (Thread/sleep 3000)
  (recur))

(defn watcher-register []
  (let [clip (.getSystemClipboard (Toolkit/getDefaultToolkit))]
  (dosync (ref-set *watcher-thread* (proxy [Thread] []
                                      (start []
                                             (proxy-super start))
                                      (run []
                                           (watcher-loop)))))
  (.start @*watcher-thread*)))

