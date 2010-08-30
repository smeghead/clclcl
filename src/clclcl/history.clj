(ns clclcl.history
  (:gen-class)
  (:use clojure.contrib.logging clclcl.options clclcl.database))
(impl-get-log (str *ns*))

(defn history-insert [s]
  (db-delete-clipboard-data s)
  (db-insert-clipboard-data s))

(defn history-get []
  (db-select-clipboard-data (get-options)))
