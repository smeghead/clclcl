(ns clclcl.watcher
  (:gen-class)
  (:use clclcl.clipboard clclcl.history clclcl.tasktray clclcl.utils)
  (:import (java.awt Toolkit)
     (java.awt.datatransfer  FlavorListener)))

(def *watcher-thread* (ref nil))

(defn watcher-loop []
  (let [s (clipboard-get)]
    (if (and s (not (empty? s)))
      (history-insert s)))
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

