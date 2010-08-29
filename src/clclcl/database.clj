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

(defn db-init []
  (.newInstance (Class/forName "org.apache.derby.jdbc.EmbeddedDriver"))
  (let [dir (File. *database-path*)]
    (if (not (.exists dir))
      (with-connection (assoc *db* :create true)
                       (create-table :clipboard_data
                                     [:id :int "not null generated always as identity constraint clipboard_data_pk primary key"]
                                     [:data_type "varchar(36)"]
                                     [:data "varchar(30000)"])))))

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
                   ;this part(fetch first ?) may be not supported bind parameter(?). so use String.format.
                   (with-query-results rs [(format "select * from clipboard_data order by id desc fetch first %d rows only" list-max)]
                                       (doall rs))))
