(ns clclcl.tasktray
  (:gen-class)
  (:use clojure.contrib.logging clclcl.options clclcl.database clclcl.clipboard clclcl.history clclcl.utils)
  (:import (java.awt SystemTray TrayIcon Image Font)
     (java.awt.datatransfer Clipboard DataFlavor StringSelection)
     (java.awt.event ActionListener MouseListener WindowFocusListener)
     (javax.imageio ImageIO)
     (javax.swing JOptionPane JFrame JPopupMenu JMenuItem)))
(impl-get-log (str *ns*))

(def *tray-icon* (ref nil))

(defn get-icon-image []
  (ImageIO/read (.getResourceAsStream (.getClass "") "/clclcl/clclcl.png")))

(defn format-entry-for-item [entry]
  (if (> (reduce + (map #(min 2 (count (.getBytes (str %)))) entry)) 40)
    (loop [chars (map #(list (min 2 (count (.getBytes (str %)))) %) entry)
           cnt 0
           acc '()]
      (let [letter-count (first (first chars))
            letter (second (first chars))]
        (debug letter-count)
        (debug letter)
        (debug acc)
        (if (> cnt 40)
          (apply str (reverse (conj acc "...")))
          (recur (rest chars) (+ cnt letter-count) (conj acc letter)))))
    entry))

(def *frame* (ref nil))

(defn setup-frame []
  (if (nil? @*frame*)
    (dosync (ref-set *frame* 
                     (let [frame (JFrame. "clclcl")]
                       (doto frame
                         (.setIconImage (get-icon-image))
                         (.addWindowFocusListener
                           (proxy [WindowFocusListener] []
                             (windowGainedFocus [e])
                             (windowLostFocus [e] (.setVisible frame false))))))))))

(defmacro register-menu-item [args & body]
  (let [[menu-item] args
        e (gensym)]
    `(doto ~menu-item
       (.addActionListener (proxy [ActionListener] []
                             (actionPerformed [~e] ~@body)))
       (.setFont (Font. (:font-name (get-options)) Font/PLAIN 14)))))

(defn display-menu [x y]
  (setup-frame)
  (doto @*frame*
    (.dispose)
    (.setUndecorated true)
    (.setBounds x y 0 0)
    (.setVisible true))
  (let [popup-menu (JPopupMenu.)]
    (loop [entries (history-get)]
      (if (not (empty? entries))
        (let [entry (first entries) 
              menu-item (JMenuItem. (format-entry-for-item (entry :data)))]
          (register-menu-item [menu-item] (clipboard-set (entry :data)))
          (.add popup-menu menu-item)
          (recur (rest entries)))))
    (.addSeparator popup-menu)
    (let [menu-item (JMenuItem. "exit")]
      (register-menu-item [menu-item]
                          (db-shutdown)
                          (java.lang.System/exit 0))
      (.add popup-menu menu-item))
    (.requestFocusInWindow popup-menu)
    (.show popup-menu (.getComponent @*frame* 0) 0 0)))

(defn tasktray-register []
  (let [tray (SystemTray/getSystemTray)]
    (dosync (ref-set *tray-icon* (TrayIcon. (get-icon-image) "clclcl")))
    (.addMouseListener @*tray-icon*
                       (proxy [MouseListener] []
                         (mousePressed [e])
                         (mouseReleased [e])
                         (mouseEntered [e])
                         (mouseExited [e])
                         (mouseClicked [e] (display-menu (.getXOnScreen e) (.getYOnScreen e)))))
    (.add tray @*tray-icon*)
    (trace "tasktray-register.")))
