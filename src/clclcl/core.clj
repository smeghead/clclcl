(ns clclcl.core
  (:gen-class))

(use 'clclcl.clipboard)
(use 'clclcl.database)
(use 'clclcl.history)
(use 'clclcl.tasktray)
(use 'clclcl.watcher)

(defn -main [& args]
  (db-init)
  (watcher-register)
  (tasktray-register))
