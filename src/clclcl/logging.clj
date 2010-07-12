(ns clclcl.logging
  (:gen-class)
  (:require clojure.contrib.logging)
  (:import (java.util.logging Level FileHandler Filter Formatter SimpleFormatter)))

(defn setup-logger []
  (let [logger (clojure.contrib.logging/impl-get-log "")]
    (println "setup-logger")
    (let [handler (java.util.logging.FileHandler. (str (System/getenv "HOME") "/.clclcl/clclcl.log") true)
          formatter (proxy [SimpleFormatter] []
                      (formatMessage [r]
                                      (str " [" (.getLoggerName r) "] "
                                           (.getMessage r))))]
      (.setFormatter handler formatter)
      (.setFilter handler (proxy [Filter] []
                            (isLoggable [record]
                                        (.startsWith (.getLoggerName record) "clclcl"))))
      (doto logger
        (.setLevel Level/ALL)
        (.addHandler handler)))))

(setup-logger)
