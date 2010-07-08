(ns clclcl.core
  (:gen-class)
  (:use clclcl.clipboard clclcl.database clclcl.history clclcl.tasktray clclcl.watcher))

(defn -main [& args]
  (db-init)
  (watcher-register)
  (tasktray-register))
