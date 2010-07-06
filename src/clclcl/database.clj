(ns clclcl.database
  (:gen-class)
  (:import (java.sql Connection DriverManager)))

(def *db* (ref '()))
(def *database-path* (str "~/.clclcl/clclcl"))
(def *create-table-sql-of* {:clipboard-data "create table clipboard_data(id int not null generated always as identity constraint clipboard_data_pk primary key, data_type varchar(36), data varchar(30000))"})

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



;(defmacro with-result-set [rs sql params body]
(defn db-select [];[sql params]
  (.newInstance (Class/forName "org.apache.derby.jdbc.EmbeddedDriver"))
  (let [conn (DriverManager/getConnection (str "jdbc:derby:" *database-path*) )
        state (.createStatement conn)
        rs (.executeQuery state "select * from clipboard_data")
        results (doall (resultset-seq rs))]
    (.close rs)
    (.close state)
    (.close conn)
    results))
