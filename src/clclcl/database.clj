(ns clclcl.database
  (:gen-class)
  (:import (java.sql Connection DriverManager)
     (java.io File)))

(def *database-path* (str (System/getenv "HOME") "/.clclcl/clclcl"))
(def *create-table-sql-of*
  {:clipboard-data "create table clipboard_data(id int not null generated always as identity constraint clipboard_data_pk primary key, data_type varchar(36), data varchar(30000))"})


;(defn db-get-first []
;  (if (> (count @*db*) 0)
;    (@*db* 0)
;    nil))


(defn db-init []
  (.newInstance (Class/forName "org.apache.derby.jdbc.EmbeddedDriver"))
  (let [dir (File. *database-path*)]
    (if (not (.exists dir))
      (let [conn (DriverManager/getConnection (str "jdbc:derby:" *database-path* ";create=true"))
            state (.createStatement conn)]
        (.execute state (*create-table-sql-of* :clipboard-data))
        (.close state)
        (.close conn)))))

(defn db-query [sql params]
  (let [conn (DriverManager/getConnection (str "jdbc:derby:" *database-path*))
            state (.createStatement conn)]
        (.execute state sql)
        (.close state)
        (.close conn)))

;(defmacro with-result-set [rs sql params body]
(defn db-select [sql params]
  (let [conn (DriverManager/getConnection (str "jdbc:derby:" *database-path*))
        state (.createStatement conn)
        rs (.executeQuery state "select * from clipboard_data")
        results (doall (resultset-seq rs))]
    (.close rs)
    (.close state)
    (.close conn)
    results))

(defn db-insert [s]
  (db-query (str "delete from clipboard_data where data = '" s "'") nil)
  (db-query (str "insert into clipboard_data(data) values ('" s "')") nil))

(defn db-get []
  (db-select "select * from clipboard_data order by id desc limit 20 offset 0" nil))
