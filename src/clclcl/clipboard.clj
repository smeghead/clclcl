(ns clclcl.clipboard
  (:gen-class)
  (:use clojure.contrib.logging)
  (:import (java.awt Toolkit)
        (java.awt.datatransfer Clipboard DataFlavor StringSelection UnsupportedFlavorException)))
(impl-get-log (str *ns*))

(defn clipboard-get []
  (let [clip (.getSystemClipboard (Toolkit/getDefaultToolkit))]
    (if (.isDataFlavorAvailable clip DataFlavor/stringFlavor)
      (try (.getData clip DataFlavor/stringFlavor)
        (catch UnsupportedFlavorException e 
          (error "failed to get data from clipboard." e))))))

(defn clipboard-set [s]
  (info (str "str:" s))
  (let [selection (StringSelection. s)
        clip (.getSystemClipboard (Toolkit/getDefaultToolkit))]
    (.setContents clip selection nil)))

