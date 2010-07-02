(ns clclcl.database
  (:gen-class))

(def db (ref []))

(defn db-insert [str]
  (dosync (ref-set db (conj @db str))))

(defn db-get []
  @db)

(defn db-get-first []
  (println (> (count @db) 0))
  (if (> (count @db) 0)
    (@db 0)
    nil))

