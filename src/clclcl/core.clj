(ns clclcl.core
  (:gen-class))

(use 'clclcl.clipboard)
(use 'clclcl.database)
(use 'clclcl.tasktray)
(use 'clclcl.watcher)

(defn -main [& args]
  (println (db-select))
  (watcher-register)
  (let [db (db-get)]
    (tasktray-register db)))
