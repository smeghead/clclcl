(ns clclcl.database
  (:gen-class)
  (:use clojure.contrib.logging clojure.contrib.sql clclcl.utils)
  (:import [org.apache.derby.jdbc EmbeddedDriver]
     [java.sql SQLException]
     [java.io File]))
(impl-get-log (str *ns*))

(def *database-path* (str (System/getenv "HOME") "/.clclcl/clclcl"))
(def *db* {:classname "org.apache.derby.jdbc.EmbeddedDriver"
           :subprotocol "derby"
           :subname *database-path*})

;(def *create-table-sql-of*
;  {:clipboard-data "create table clipboard_data(id bigint not null generated always as identity constraint clipboard_data_pk primary key, data_type varchar(36), data varchar(30000))"})

;(defn db-setup-params [state params]
;  (if params
;    (loop [ps params
;           i 1]
;      (if (not (empty? ps))
;        (let []
;          (.setObject state i (first ps))
;          (recur (rest ps) (inc i))))))
;  state)

;(defmacro with-statement [args & body]
;  (let [[state sql params option] args
;        database-path *database-path*
;        database-option (or option "")
;        conn (gensym)
;        result (gensym)]
;    `(let [~conn (DriverManager/getConnection (str "jdbc:derby:" ~database-path ~database-option))
;           ~state (db-setup-params (.prepareStatement ~conn ~sql) ~params)
;           ~result (let [] 
;                     ~@body)]
;       (.close ~state)
;       (.close ~conn)
;       ~result)))

(defn db-init []
  (.newInstance (Class/forName "org.apache.derby.jdbc.EmbeddedDriver"))
  (let [dir (File. *database-path*)]
    (if (not (.exists dir))
      (with-connection (assoc *db* :create true)
                       (create-table :clipboard_data
                                     [:id :big-int "not null generated always as identity constraint clipboard_data_pk primary key"]
                                     [:data_type "varchar(36)"]
                                     [:data "varchar(30000))"])))))

(defn db-shutdown []
  (try
    (with-connection (assoc *db* :shutdown true))
    (catch SQLException e
      (println "derby shutdown."))))

(defn db-delete-clipboard-data [s]
  (with-connection *db*
                   (delete-rows :clipboard_data ["data=?" s])))

(defn db-insert-clipboard-data [s]
  (with-connection *db*
                   (insert-values :clipboard_data [:data] [s])))

(defn db-select-clipboard-data [{list-max :list-max}]
  (with-connection *db*
                   ;bind parameter(?) got syntax error. so use String.format.
                   (with-query-results rs [(format "select * from clipboard_data order by id desc fetch first %d rows only" list-max)]
                                       (doall rs))))
