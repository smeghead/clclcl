(ns clclcl.tasktray
  (:gen-class)
  (:use clojure.contrib.logging clojure.test clclcl.options clclcl.database clclcl.clipboard clclcl.history clclcl.utils clclcl.templates)
  (:import (java.awt SystemTray TrayIcon Image Font)
     (java.awt.datatransfer Clipboard DataFlavor StringSelection)
     (java.awt.event ActionListener MouseListener WindowFocusListener)
     (javax.imageio ImageIO)
     (javax.swing JOptionPane JFrame JPopupMenu JMenuItem JMenu UIManager)))
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
                             (actionPerformed [~e] ~@body))))))

(defn register-menu-items [entries popup-menu]
  (loop [items entries]
    (if-not (empty? items)
      (let [item (first items) 
            data (item :data)
            string-data (if (list? data) ((eval data)) data) ; eval form was written in templates.clj
            menu-item (JMenuItem. (format-entry-for-item (or (item :name) string-data)))]
        (debug "------------------")
        (debug string-data)
        (if-not (= (.getText menu-item) string-data)
          (.setToolTipText menu-item string-data))
        (register-menu-item [menu-item] (clipboard-set string-data))
        (.add popup-menu menu-item)
        (recur (rest items))))))

(defn display-menu [x y]
  (try
    (setup-frame)
    (doto @*frame*
      (.dispose)
      (.setUndecorated true)
      (.setBounds x y 0 0)
      (.setVisible true))
    (let [popup-menu (JPopupMenu.)]
      ;history
      (register-menu-items (history-get) popup-menu)
      (.addSeparator popup-menu)
      ;templates
      (let [templates (templates-get)]
        (let [template-menu-item (JMenu. "Registerd Templates")]
          (register-menu-item [template-menu-item])
          (.add popup-menu template-menu-item)
          (register-menu-items (templates-get) template-menu-item)))
      (.addSeparator popup-menu)
      ;exit
      (let [menu-item (JMenuItem. "exit")]
        (register-menu-item [menu-item]
                            (db-shutdown)
                            (java.lang.System/exit 0))
        (.add popup-menu menu-item))
      (.requestFocusInWindow popup-menu)
      (.show popup-menu (.getComponent @*frame* 0) 0 0))
    (catch Exception e
      (error "error occured when display-menu." e))))

(defn tasktray-register []
  ;set default font.
  (UIManager/put "ToolTip.font" (Font. (:font-name (get-options)) Font/PLAIN 12))
  (UIManager/put "MenuItem.font" (Font. (:font-name (get-options)) Font/PLAIN (:font-size (get-options))))
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
