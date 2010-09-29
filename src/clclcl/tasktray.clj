(ns clclcl.tasktray
  (:gen-class)
  (:use clojure.contrib.logging clojure.test clclcl.options clclcl.database clclcl.clipboard clclcl.history clclcl.utils clclcl.templates clclcl.server)
  (:import (java.awt SystemTray TrayIcon Image Font MouseInfo Robot)
     (java.awt.datatransfer Clipboard DataFlavor StringSelection)
     (java.awt.event ActionListener MouseListener WindowFocusListener KeyEvent)
     (javax.imageio ImageIO)
     (javax.swing JOptionPane JFrame JPopupMenu JMenuItem JMenu UIManager)
     (javax.swing.event PopupMenuListener MenuKeyListener)))
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
                         (.setIconImage (get-icon-image))))))))

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
            loaded-item (cond
                            (coll? item) item
                            :else {:name item :data item})
            name (loaded-item :name)
            data (loaded-item :data)
            menu-item (JMenuItem. (format-entry-for-item name))]
        (if-not (= (.getText menu-item) (str data))
          (.setToolTipText menu-item (str data)))
        (register-menu-item [menu-item] (clipboard-set (if (list? data)
                                                         ((eval data)) ;eval form was written in templates.clj
                                                         data)))
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
    (let [popup-menu (JPopupMenu.)
          robot (Robot.)]
      ;register keybind.
      (.addMenuKeyListener popup-menu
                           (proxy [MenuKeyListener] []
                             (menuKeyTyped [e])
                             (menuKeyPressed [e]
                                             (cond
                                               (and (= (.getKeyCode e) KeyEvent/VK_OPEN_BRACKET) (.isControlDown e)) (.setVisible popup-menu false)
                                               (and (= (.getKeyCode e) KeyEvent/VK_M) (.isControlDown e)) (do
                                                                                                            (.keyPress robot KeyEvent/VK_ENTER)
                                                                                                            (.keyRelease robot KeyEvent/VK_ENTER))
                                               (= (.getKeyCode e) KeyEvent/VK_H) (.keyPress robot KeyEvent/VK_LEFT)
                                               (= (.getKeyCode e) KeyEvent/VK_L) (.keyPress robot KeyEvent/VK_RIGHT)
                                               (= (.getKeyCode e) KeyEvent/VK_J) (.keyPress robot KeyEvent/VK_DOWN)
                                               (= (.getKeyCode e) KeyEvent/VK_K) (.keyPress robot KeyEvent/VK_UP)))
                             (menuKeyReleased [e]
                                             (cond
                                               (= (.getKeyCode e) KeyEvent/VK_H) (.keyRelease robot KeyEvent/VK_LEFT)
                                               (= (.getKeyCode e) KeyEvent/VK_L) (.keyRelease robot KeyEvent/VK_RIGHT)
                                               (= (.getKeyCode e) KeyEvent/VK_J) (.keyRelease robot KeyEvent/VK_DOWN)
                                               (= (.getKeyCode e) KeyEvent/VK_K) (.keyRelease robot KeyEvent/VK_UP)))))
      ;history
      (register-menu-items (history-get) popup-menu)
      (.addSeparator popup-menu)
      ;templates
      (let [template-menu-item (JMenu. "Registerd Templates")]
        (register-menu-item [template-menu-item])
        (.add popup-menu template-menu-item)
        (register-menu-items (templates-get) template-menu-item))
      (.addSeparator popup-menu)
      ;exit
      (let [menu-item (JMenuItem. "exit")]
        (register-menu-item [menu-item]
                            (db-shutdown)
                            (stop-server) ; stop listen server.
                            (java.lang.System/exit 0))
        (.add popup-menu menu-item))
      (.requestFocusInWindow popup-menu)
      (.addPopupMenuListener popup-menu
                             (proxy [PopupMenuListener] []
                               (popupMenuCanceled [e])
                               (popupMenuWillBecomeInvisible [e](.setVisible @*frame* false ))
                               (popupMenuWillBecomeVisible [e])))
      (.show popup-menu (.getComponent @*frame* 0) 0 0))
    (catch Exception e
      (error "error occured when display-menu." e)
      (throw e))))

(defn tasktray-register []
  ;listen server start
  (start-server (fn listen-fn [in out]
                  (let [point (.getLocation (MouseInfo/getPointerInfo))]
                    (display-menu (.x point) (.y point)))))
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
