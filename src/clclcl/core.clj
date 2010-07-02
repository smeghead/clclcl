(ns clclcl.core
  (:gen-class))

(use 'clclcl.clipboard)
(use 'clclcl.database)
(use 'clclcl.tasktray)

(defn -main [& args]
  (println (str "Hello, " (db-get-first)))
  (db-insert "aaa")
  (db-insert "bbb")
  (db-insert "ccc")
  (db-insert (clipboard-get))
  (let [db (db-get)]
    (println (str "Hello, " (db 0)))
    (println (str "Hello, " (db 1)))
    (println (str "Hello, " (db 2)))
    (println (str "Hello, " (db 3)))
    (tasktray-register db))
  (println (str "Hello, " (db-get-first))))
