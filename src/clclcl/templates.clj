(ns clclcl.templates
  (:gen-class)
  (:use [clojure.contrib.duck-streams :only (reader with-out-writer)]
     clojure.contrib.logging)
  (:import [java.io File]))
(impl-get-log (str *ns*))

(def *user-templates-file* (str (System/getenv "HOME") "/.clclcl/templates.clj"))

(defn templates-get []
  (try
    (if-not (.. (File. *user-templates-file*) exists)
      (with-out-writer *user-templates-file* (prn [])))
    (read (java.io.PushbackReader. (reader *user-templates-file*)));)
    (catch Exception e
      (error "failed to read user templates file." e)
      [])))

(defn templates-match [s]
  (some (fn [x]
          (= s (if (map? x)
                 (x :data)
                 x)))
        (templates-get)))

