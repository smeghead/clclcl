(ns clclcl.watcher
  (:gen-class)
  (:use clojure.contrib.logging clclcl.options clclcl.clipboard clclcl.history clclcl.tasktray clclcl.utils))
(impl-get-log (str *ns*))

(defn watcher-loop []
  (try
    (let [s (clipboard-get)]
      (if (and s
               (not (empty? s))
               (not (some (fn [x] (> (.indexOf s x) -1)) (:ignore-words (get-options)))))
        (do
          (history-insert s)
          (history-update))))
    (catch Exception e
      (error "watcher-loop failed." e)))
  (Thread/sleep (:watch-interval (get-options)))
  (recur))

(defn watcher-register []
  (trace "watcher thread start.")
  (.start (proxy [Thread] []
            (start [] (proxy-super start))
            (run [] (watcher-loop)))))

