(ns clclcl.core
  (:gen-class))

(use 'clclcl.clipboard)
(use 'clclcl.database)

(defn -main [& args]
  (db-insert "aaa")
  (db-insert "bbb")
  (db-insert "ccc")
  (db-insert (get-clipboard))
  (let [db (db-get)]
    (println (str "Hello, " (db 0)))
    (println (str "Hello, " (db 1)))
    (println (str "Hello, " (db 2)))
    (println (str "Hello, " (db 3)))))
