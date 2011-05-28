(ns clclcl.tasktray
  (:gen-class)
  (:use clojure.contrib.logging clojure.test clclcl.options clclcl.database clclcl.clipboard clclcl.history clclcl.utils clclcl.templates clclcl.server)
  (:import 
     (org.eclipse.swt SWT)
     (org.eclipse.swt.widgets Display Tray TrayItem Shell Menu MenuItem Listener)
     (org.eclipse.swt.graphics Device Image)
     (org.eclipse.swt.events SelectionAdapter)
     (javax.imageio ImageIO)))
(impl-get-log (str *ns*))

(def *tray-icon* (ref nil))

(defn get-icon-image-stream []
  (.getResourceAsStream (.getClass "") "/clclcl/clclcl.png"))

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

(defmacro register-menu-item [args & body]
  (let [[menu-item] args
        e (gensym)]
    `(doto ~menu-item
       (.addSelectionListener (proxy [SelectionAdapter] []
                             (widgetSelected [~e] ~@body))))))

(defn register-menu-items [entries popup-menu]
  (loop [items entries]
    (if-not (empty? items)
      (let [item (first items) 
            loaded-item (cond
                            (coll? item) item
                            :else {:name item :data item})
            name (loaded-item :name)
            data (loaded-item :data)
            menu-item (MenuItem. popup-menu SWT/PUSH)]
        (.setText menu-item (format-entry-for-item name))
;        (if-not (= (.getText menu-item) (str data))
;          (.setToolTipText menu-item (str data)))
        (register-menu-item [menu-item]
                            (clipboard-set (if (list? data)
                                             ((eval data)) ;eval form was written in templates.clj
                                             data)))
        (recur (rest items))))))

(defn display-menu [shell]
  (try
    (let [popup-menu (Menu. shell  SWT/POP_UP)]
      ;register keybind.
;      (.addMenuKeyListener popup-menu
;                           (proxy [MenuKeyListener] []
;                             (menuKeyTyped [e])
;                             (menuKeyPressed [e]
;                                             (cond
;                                               (and (= (.getKeyCode e) KeyEvent/VK_OPEN_BRACKET) (.isControlDown e)) (.setVisible popup-menu false)
;                                               (and (= (.getKeyCode e) KeyEvent/VK_M) (.isControlDown e)) (do
;                                                                                                            (.keyPress robot KeyEvent/VK_ENTER)
;                                                                                                            (.keyRelease robot KeyEvent/VK_ENTER))
;                                               (= (.getKeyCode e) KeyEvent/VK_H) (.keyPress robot KeyEvent/VK_LEFT)
;                                               (= (.getKeyCode e) KeyEvent/VK_L) (.keyPress robot KeyEvent/VK_RIGHT)
;                                               (= (.getKeyCode e) KeyEvent/VK_J) (.keyPress robot KeyEvent/VK_DOWN)
;                                               (= (.getKeyCode e) KeyEvent/VK_K) (.keyPress robot KeyEvent/VK_UP)))
;                             (menuKeyReleased [e]
;                                             (cond
;                                               (= (.getKeyCode e) KeyEvent/VK_H) (.keyRelease robot KeyEvent/VK_LEFT)
;                                               (= (.getKeyCode e) KeyEvent/VK_L) (.keyRelease robot KeyEvent/VK_RIGHT)
;                                               (= (.getKeyCode e) KeyEvent/VK_J) (.keyRelease robot KeyEvent/VK_DOWN)
;                                               (= (.getKeyCode e) KeyEvent/VK_K) (.keyRelease robot KeyEvent/VK_UP)))))
      ;history
      (register-menu-items (history-get) popup-menu)
;      (.addSeparator popup-menu)
      ;templates
;      (let [template-menu-item (JMenu. "Registerd Templates")]
;        (register-menu-item [template-menu-item])
;        (.add popup-menu template-menu-item)
;        (register-menu-items (templates-get) template-menu-item))
;      (.addSeparator popup-menu)
      ;exit
      (let [menu-item (MenuItem. popup-menu SWT/PUSH)]
        (.setText menu-item "Exit")
        (register-menu-item [menu-item]
                            (java.lang.System/exit 0))
        )
      (.setVisible popup-menu true))
    ))

(defn tasktray-register []
  (let [display (Display.)
        shell (Shell. display)
        tray (.getSystemTray display)
        tray-item (TrayItem. tray SWT/NONE)]
    (doto tray-item
      (.setToolTipText "clclcl")
      (.setImage (Image. display (get-icon-image-stream)))
      (.addListener SWT/MenuDetect (proxy [Listener] []
                                     (handleEvent [e]
                                                  (info "selected")
                                                  (display-menu shell))
                                     )))
    (loop []
      (if (.readAndDispatch display)
        (do
          (.sleep display)
          (recur))
        (.dispose display))))
  (info "ok")

;  (let [tray (SystemTray/getSystemTray)]
;    (dosync (ref-set *tray-icon* (TrayIcon. (get-icon-image) "clclcl")))
;    (.addMouseListener @*tray-icon*
;                       (proxy [MouseListener] []
;                         (mousePressed [e])
;                         (mouseReleased [e])
;                         (mouseEntered [e])
;                         (mouseExited [e])
;                         (mouseClicked [e] (display-menu (.getXOnScreen e) (.getYOnScreen e)))))
;    (.add tray @*tray-icon*)
;    (trace "tasktray-register."))
  )
