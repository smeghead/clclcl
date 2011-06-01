(ns clclcl.server
  (:gen-class)
  (:use clojure.contrib.server-socket clojure.contrib.logging clclcl.options))
(impl-get-log (str *ns*))

(def *server* (atom nil))


(defn start-server [fn]
  (debug (str "start server " (:server-port (get-options))))
  (reset! *server*
          (create-server (:server-port (get-options)) fn)))

(defn stop-server []
  (debug "stop server")
  (when-not (nil? @*server*)
    (close-server @*server*)
    (reset! *server* nil)))

