(ns clclcl.history
  (:gen-class)
  (:use clojure.contrib.logging clclcl.options clclcl.database clclcl.templates))
(impl-get-log (str *ns*))

(def *history-items* (ref nil))

(defn history-insert [s]
  (if-not (templates-match s)
    (do
      (db-delete-clipboard-data s)
      (db-insert-clipboard-data s))))

(defn history-update []
  (dosync (ref-set *history-items*
                  (db-select-clipboard-data (get-options))))
  @*history-items*)

(defn history-get []
  (or @*history-items*
      (history-update)))
