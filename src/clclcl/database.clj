(ns clclcl.database
  (:gen-class)
  (:use clclcl.utils)
  (:import (java.sql Connection DriverManager)
     (java.io File)))

(def *database-path* (str (System/getenv "HOME") "/.clclcl/clclcl"))
(def *create-table-sql-of*
  {:clipboard-data "create table clipboard_data(id int not null generated always as identity constraint clipboard_data_pk primary key, data_type varchar(36), data varchar(30000))"})

(defn db-init []
  (.newInstance (Class/forName "org.apache.derby.jdbc.EmbeddedDriver"))
  (let [dir (File. *database-path*)]
    (if (not (.exists dir))
      (let [conn (DriverManager/getConnection (str "jdbc:derby:" *database-path* ";create=true"))
            state (.prepareStatement conn (*create-table-sql-of* :clipboard-data))]
        (.execute state)
        (.close state)
        (.close conn)))))

(defn db-setup-params [state params]
  (if params
    (loop [ps params
           i 1]
      (if (plus? (count ps))
        (let [p (first ps)]
          (.setObject state i p)
          (recur (rest ps) (inc i))))))
  state)

(defn db-query [sql params]
  (let [conn (DriverManager/getConnection (str "jdbc:derby:" *database-path*))
            state (db-setup-params (.prepareStatement conn sql) params)]
        (.execute state)
        (.close state)
        (.close conn)))

(defn db-select [sql params]
  (let [conn (DriverManager/getConnection (str "jdbc:derby:" *database-path*))
        state (db-setup-params (.prepareStatement conn sql) params)
        rs (.executeQuery state)
        results (doall (resultset-seq rs))]
    (.close rs)
    (.close state)
    (.close conn)
    results))
