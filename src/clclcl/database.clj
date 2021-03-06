(ns clclcl.database
  (:gen-class)
  (:use clojure.contrib.logging clojure.contrib.sql clclcl.utils)
  (:import [java.sql SQLException]
     [java.io File]))
(impl-get-log (str *ns*))

(def *database-dir* (str (System/getenv "HOME") "/.clclcl"))
(def *database-path* (str (System/getenv "HOME") "/.clclcl/clclcl.db"))
(def *db* {:classname "org.sqlite.JDBC"
           :subprotocol "sqlite"
           :subname *database-path*})

(defn db-init []
  (.newInstance (Class/forName "org.sqlite.JDBC"))
  (let [dir (File. *database-path*)]
    (if-not (.exists dir)
      (do
        (.mkdir (File. *database-dir*))
        (with-connection (assoc *db* :create true)
                         (create-table :clipboard_data
                                       [:id :int "primary key"]
                                       [:data_type "varchar(36)"]
                                       [:data "varchar(30000)"]))))))

(defn db-delete-clipboard-data [s]
  (with-connection *db*
                   (delete-rows :clipboard_data ["data=?" s])))

(defn db-insert-clipboard-data [s]
  (with-connection *db*
                   (insert-values :clipboard_data [:data] [s])))

(defn db-select-clipboard-data [{list-max :list-max}]
  (with-connection *db*
                   (with-query-results rs ["select *, data as name from clipboard_data order by id desc limit ? offset 0" list-max]
                                       (doall rs))))
