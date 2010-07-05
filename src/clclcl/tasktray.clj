(ns clclcl.tasktray
  (:gen-class))

(import (java.awt SystemTray TrayIcon PopupMenu MenuItem Image Font)
        (java.awt.datatransfer Clipboard DataFlavor StringSelection)
        (java.awt.event ActionListener MouseListener)
        (javax.imageio ImageIO)
        (javax.swing JOptionPane))

(use 'clclcl.clipboard)
(use 'clclcl.database)

(def *tray-icon* (ref nil))

(defn plus? [x]
  (> x 0))

(defn create-menu [db]
  (let [popup-menu (PopupMenu.)]
    (loop [entries (take 20 db)]
      (if (plus? (count entries))
        (let [entry (first entries) 
              menu-item (MenuItem. (if (> (count entry) 20)
                                     (subs entry 0 20)
                                     entry))]
          (doto menu-item
            (.addActionListener 
              (proxy [ActionListener] []
                (actionPerformed [e] (clipboard-set entry))))
            (.setFont (Font. "VL Pゴシック" Font/PLAIN 14)))
          (.add popup-menu menu-item)
          (recur (rest entries)))))
    popup-menu))

(defn tasktray-register [db]
  (let [tray (SystemTray/getSystemTray)
        tray-icon (TrayIcon.
                    (ImageIO/read (.getResourceAsStream (.getClass tray) "/clclcl/clclcl.png"))
                    "clclcl"
                    (create-menu db))]
    (dosync (ref-set *tray-icon* tray-icon))
    (.addActionListener tray-icon
                        (proxy [ActionListener] []
                          (actionPerformed [e]
                                           (JOptionPane/showMessageDialog nil "I will exit.")
                                           (java.lang.System/exit 0))))
    (.add tray tray-icon)))

(defn tasktray-update-menu []
  (let [db (db-get)]
    (if (and @*tray-icon* db)
      (.setPopupMenu @*tray-icon* (create-menu db)))))
