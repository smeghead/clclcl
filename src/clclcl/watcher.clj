(ns clclcl.watcher
  (:gen-class))

(import (java.awt Toolkit)
        (java.awt.datatransfer Clipboard DataFlavor StringSelection))

(use 'clclcl.clipboard)
(use 'clclcl.database)
(use 'clclcl.tasktray)

(def *watcher-thread* (ref nil))

(defn watcher-loop []
  (let [str (clipboard-get)]
    (if str
      (db-insert str)))
  (tasktray-update-menu)
  (Thread/sleep 3000)
  (recur))

(defn watcher-register []
  (dosync (ref-set *watcher-thread* (proxy [Thread] []
                                      (start []
                                             (proxy-super start))
                                      (run []
                                           (watcher-loop)))))
  (.start @*watcher-thread*))
