(ns clclcl.options
  (:gen-class)
  (:use [clojure.contrib.duck-streams :only (reader with-out-writer)]
     clojure.contrib.logging)
  (:import [java.io File]))
(impl-get-log (str *ns*))

(def *user-setting-file* (str (System/getenv "HOME") "/.clclcl/clclcl.conf"))

(def *default-option-values* {:list-max 30
                              :watch-interval 3000
                              :server-port 10000
                              :ignore-words []})

(defn get-options []
  (try
    (if-not (.. (File. *user-setting-file*) exists)
      (with-out-writer *user-setting-file* (prn *default-option-values*)))
    (let [user-settings (read (java.io.PushbackReader. (reader *user-setting-file*)))]
      (loop [options {}
             keys (keys *default-option-values*)]
        (if (> (count keys) 0)
          (let [k (first keys)]
            (recur (assoc options k (or (user-settings k) (*default-option-values* k)))
                   (rest keys)))
          options)))
    (catch Exception e
      (error "failed to read user settings file." e)
      *default-option-values*)))
