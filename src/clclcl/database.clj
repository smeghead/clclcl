(ns clclcl.database
  (:gen-class))

(def db (ref []))

(defn db-insert [str]
  (dosync (ref-set db (conj @db str))))

(defn db-get []
  @db)

