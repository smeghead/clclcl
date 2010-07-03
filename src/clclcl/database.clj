(ns clclcl.database
  (:gen-class))

(def *db* (ref '()))

(defn db-insert [str]
  (println @*db*)
  (dosync
    (let [db (remove #(= % str) @*db*)]
      (ref-set *db* (conj db str)))))

(defn db-get []
  @*db*)

(defn db-get-first []
  (if (> (count @*db*) 0)
    (@*db* 0)
    nil))

