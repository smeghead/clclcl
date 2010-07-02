(ns clclcl.tasktray
  (:gen-class))

(import (java.awt SystemTray TrayIcon PopupMenu MenuItem Image)
        (java.awt.datatransfer Clipboard DataFlavor StringSelection)
        (java.awt.event ActionListener)
        (javax.imageio ImageIO))

(defn create-menu [db]
  (let [popup-menu (PopupMenu. "menu")]
    (doto popup-menu
      (.add (MenuItem. "aaa"))
      (.add (MenuItem. "bbb")))
    popup-menu))


(defn tasktray-register [db]
  (let [tray (SystemTray/getSystemTray)
        tray-icon (TrayIcon.
                    (ImageIO/read (.getResourceAsStream (.getClass tray) "/clclcl/clclcl.png"))
                    "clclcl"
                    (create-menu db))]
    (.addActionListener tray-icon
                        (proxy [ActionListener] []
                          (actionPerformed [e] (println "ofu"))))
    (.add tray tray-icon)))

;(defn tasktray-register []
;  (let [tray (SystemTray/getSystemTray)
;        tray-icon (TrayIcon. (ImageIO/read (.getResourceAsStream (.getClass tray) "/clclcl/clclcl.png")))]
;    (.setToolTip tray-icon "clclcl")
;    (.add tray tray-icon)))

