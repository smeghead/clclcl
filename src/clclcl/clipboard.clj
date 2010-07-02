(ns clclcl.clipboard
  (:gen-class))

(import (java.awt Toolkit)
        (java.awt.datatransfer Clipboard DataFlavor StringSelection))

(defn get-clipboard []
  (let [clip (.getSystemClipboard (Toolkit/getDefaultToolkit))]
    (.getData clip DataFlavor/stringFlavor)))

(defn set-clipboard [str]
  (let [selection (StringSelection. str)
        clip (.getSystemClipboard (Toolkit/getDefaultToolkit))]
    (.setContents clip selection nil)))

