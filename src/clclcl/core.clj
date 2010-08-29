(ns clclcl.core
  (:gen-class)
  (:use clclcl.clipboard clclcl.database clclcl.history clclcl.tasktray clclcl.watcher
     clojure.contrib.logging))
(impl-get-log (str *ns*))

(defn -main [& args]
  (info "start clclcl.")
  (db-init)
  (history-get) ; initial database access is very late. so execute dummy access here.
  (watcher-register)
  (tasktray-register))
