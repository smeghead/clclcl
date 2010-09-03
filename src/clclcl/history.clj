(ns clclcl.history
  (:gen-class)
  (:use clojure.contrib.logging clclcl.options clclcl.database clclcl.templates))
(impl-get-log (str *ns*))

(defn history-insert [s]
  (if-not (templates-match s)
    (do
      (db-delete-clipboard-data s)
      (db-insert-clipboard-data s))))

(defn history-get []
  (db-select-clipboard-data (get-options)))
