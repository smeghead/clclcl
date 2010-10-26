(ns clclcl.watcher
  (:gen-class)
  (:use clojure.contrib.logging clclcl.options clclcl.clipboard clclcl.history clclcl.tasktray clclcl.utils)
  (:import (java.awt Toolkit)
     (java.awt.datatransfer  FlavorListener)))
(impl-get-log (str *ns*))

(def *watcher-thread* (ref nil))

(defn watcher-loop []
  (try
    (let [s (clipboard-get)]
      (if (and s (not (empty? s)))
        (do
          (history-insert s)
          (history-update))))
    (catch Exception e
      (error "watcher-loop failed." e)))
  (Thread/sleep (:watch-interval (get-options)))
  (recur))

(defn watcher-register []
  (let [clip (.getSystemClipboard (Toolkit/getDefaultToolkit))]
    (dosync (ref-set *watcher-thread* (proxy [Thread] []
                                        (start [] (proxy-super start))
                                        (run [] (watcher-loop)))))
    (trace "watcher thread start.")
    (.start @*watcher-thread*)))

