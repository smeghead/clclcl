(ns clclcl.core
  (:gen-class)
  (:use clclcl.logging clclcl.clipboard clclcl.database clclcl.history clclcl.tasktray clclcl.watcher
     clojure.contrib.logging))
(impl-get-log (str *ns*))

(defn -main [& args]
  (trace "main start.")
  (db-init)
  (watcher-register)
  (tasktray-register))
