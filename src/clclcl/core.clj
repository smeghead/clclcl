(ns clclcl.core
  (:gen-class))

(import (java.awt Toolkit)
        (java.awt.datatransfer Clipboard DataFlavor))

(defn -main [& args]
  (let [clip (.getSystemClipboard (Toolkit/getDefaultToolkit))]
    (println (str "Hello, " (.getData clip DataFlavor/stringFlavor)))))
