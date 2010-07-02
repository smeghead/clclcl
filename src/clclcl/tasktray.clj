(ns clclcl.tasktray
  (:gen-class))

(import (java.awt SystemTray TrayIcon PopupMenu MenuItem Image)
        (java.awt.datatransfer Clipboard DataFlavor StringSelection)
        (java.awt.event ActionListener)
        (javax.imageio ImageIO)
        (javax.swing JOptionPane))

(use 'clclcl.clipboard)

(defn create-menu [db]
  (let [popup-menu (PopupMenu.)]
    (let [menu-item (MenuItem. "aaa")]
      (.addActionListener menu-item (proxy [ActionListener] [] (actionPerformed [e] (clipboard-set "aaa"))))
      (.add popup-menu menu-item))
    (let [menu-item (MenuItem. "bbb")]
      (.addActionListener menu-item (proxy [ActionListener] [] (actionPerformed [e] (clipboard-set "bbb"))))
      (.add popup-menu menu-item))
    popup-menu))


(defn tasktray-register [db]
  (let [tray (SystemTray/getSystemTray)
        tray-icon (TrayIcon.
                    (ImageIO/read (.getResourceAsStream (.getClass tray) "/clclcl/clclcl.png"))
                    "clclcl"
                    (create-menu db))]
    (.addActionListener tray-icon
                        (proxy [ActionListener] []
                          (actionPerformed [e]
                                           (println "ofu")
                                           (JOptionPane/showMessageDialog nil "Eggs are not supposed to be green."))))
    (.add tray tray-icon)))

;(defn tasktray-register []
;  (let [tray (SystemTray/getSystemTray)
;        tray-icon (TrayIcon. (ImageIO/read (.getResourceAsStream (.getClass tray) "/clclcl/clclcl.png")))]
;    (.setToolTip tray-icon "clclcl")
;    (.add tray tray-icon)))

