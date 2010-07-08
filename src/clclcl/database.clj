(ns clclcl.database
  (:gen-class)
  (:use clclcl.utils)
  (:import (java.sql Connection DriverManager)
     (java.io File)))

(def *database-path* (str (System/getenv "HOME") "/.clclcl/clclcl"))
(def *create-table-sql-of*
  {:clipboard-data "create table clipboard_data(id bigint not null generated always as identity constraint clipboard_data_pk primary key, data_type varchar(36), data varchar(30000))"})

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
      (if (not (empty? ps))
        (let [p (first ps)]
          (.setObject state i p)
          (recur (rest ps) (inc i))))))
  state)

(defmacro with-statement [args & body]
  (let [[state sql params] args
        database-path *database-path*
        conn (gensym)
        result (gensym)]
    `(let [~conn (DriverManager/getConnection (str "jdbc:derby:" ~database-path))
           ~state (db-setup-params (.prepareStatement ~conn ~sql) ~params)
           ~result (let [] 
                     ~@body)]
       (.close ~state)
       (.close ~conn)
       ~result)))

(defn db-query [sql params]
  (with-statement [state sql params]
                  (.execute state)))

(defn db-select [sql params]
  (with-statement [state sql params]
                  (let [rs (.executeQuery state)
                        results (doall (resultset-seq rs))]
                    (.close rs)
                    results)))
