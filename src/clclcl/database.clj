(ns clclcl.database
  (:gen-class)
  (:use clclcl.utils)
  (:import (java.sql Connection DriverManager)
     (java.io File)))

(def *database-path* (str (System/getenv "HOME") "/.clclcl/clclcl"))
(def *create-table-sql-of*
  {:clipboard-data "create table clipboard_data(id bigint not null generated always as identity constraint clipboard_data_pk primary key, data_type varchar(36), data varchar(30000))"})

(defn db-setup-params [state params]
  (if params
    (loop [ps params
           i 1]
      (if (not (empty? ps))
        (let []
          (.setObject state i (first ps))
          (recur (rest ps) (inc i))))))
  state)

(defmacro with-statement [args & body]
  (let [[state sql params option] args
        database-path *database-path*
        database-option (or option "")
        conn (gensym)
        result (gensym)]
    `(let [~conn (DriverManager/getConnection (str "jdbc:derby:" ~database-path ~database-option))
           ~state (db-setup-params (.prepareStatement ~conn ~sql) ~params)
           ~result (let [] 
                     ~@body)]
       (.close ~state)
       (.close ~conn)
       ~result)))

(defn db-init []
  (.newInstance (Class/forName "org.apache.derby.jdbc.EmbeddedDriver"))
  (let [dir (File. *database-path*)]
    (if (not (.exists dir))
      (with-statement [state (*create-table-sql-of* :clipboard-data) nil ";create=true"]
                      (.execute state)))))

(defn db-query [sql params]
  (with-statement [state sql params]
                  (.execute state)))

(defn db-select [sql params]
  (with-statement [state sql params]
                  (let [rs (.executeQuery state)
                        results (doall (resultset-seq rs))]
                    (.close rs)
                    results)))
