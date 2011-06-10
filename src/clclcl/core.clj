(ns clclcl.core
  (:gen-class)
  (:use clclcl.clipboard clclcl.database clclcl.history clclcl.tasktray clclcl.watcher
     clojure.contrib.logging))
(impl-get-log (str *ns*))

(defn -main [& args]
  (info "start clclcl.")
  (db-init)
  (watcher-register)
  (tasktray-register)
  (System/exit 0))
