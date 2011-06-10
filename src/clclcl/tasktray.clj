(ns clclcl.tasktray
  (:gen-class)
  (:use clojure.contrib.logging clojure.test clclcl.options clclcl.database clclcl.clipboard clclcl.history clclcl.utils clclcl.templates clclcl.server)
  (:import 
     (java.io PrintWriter)
     (org.eclipse.swt SWT)
     (org.eclipse.swt.widgets Event Display Tray TrayItem Shell Menu MenuItem Listener Composite MessageBox ToolTip Tree TreeItem)
     (org.eclipse.swt.graphics Device Image)
     (org.eclipse.swt.events SelectionAdapter KeyListener)
     (javax.imageio ImageIO)))
(impl-get-log (str *ns*))

(defn get-icon-image-stream []
  (.getResourceAsStream (.getClass "") "/clclcl/clclcl.png"))

(defn format-entry-for-item [entry]
  (let [ent (-> entry
              (.replaceAll "\n" "")
              (.replaceAll "[ \t]+" " "))
        max-length 50]
    (if (> (reduce + (map #(min 2 (count (.getBytes (str %)))) ent)) max-length)
      (loop [chars (map #(list (min 2 (count (.getBytes (str %)))) %) ent)
             cnt 0
             acc '()]
        (let [letter-count (first (first chars))
              letter (second (first chars))]
          (if (> cnt max-length)
            (apply str (reverse (conj acc "...")))
            (recur (rest chars) (+ cnt letter-count) (conj acc letter)))))
      ent)))

(defn show-center [shell]
  (let [shellRect (.getBounds shell)
        dispRect (.. shell getDisplay getBounds)]
    (.setLocation shell
                  (/ (- (.width dispRect) (.width shellRect)) 2)
                  (/ (- (.height dispRect) (.height shellRect)) 2)))
  (.setVisible shell true))

;(defmacro register-menu-item [args & body]
;  (let [[tree] args
;        e (gensym)]
;    `(doto ~tree
;       ;.addSelectionListener
;       (.addListener SWT/DefaultSelection (proxy [Listener] []
;                                     (handleEvent [~e] ~@body))))))

(defn register-menu-items [entries tree]
  (loop [items entries]
    (if-not (empty? items)
      (let [item (first items) 
            loaded-item (cond
                            (coll? item) item
                            :else {:name item :data item})
            name (loaded-item :name)
            data (loaded-item :data)
            ;menu-item (MenuItem. tree SWT/PUSH)]
            ]
        (let [item (TreeItem. tree SWT/NULL)]
          (.setText item (format-entry-for-item name)))
;        (if-not (= (.getText menu-item) (str data))
;          (.setToolTipText menu-item (str data)))
;        (register-menu-item [tree]
;                            (clipboard-set (if (list? data)
;                                             ((eval data)) ;eval form was written in templates.clj
;                                             data)))
        (recur (rest items))))))

(defn display-menu [shell]
  (let [tree (Tree. shell (bit-or SWT/BORDER SWT/V_SCROLL))
        client-area (.getClientArea shell)]
    (.setBounds tree (. client-area x) (. client-area y) 500 600)
    ;register keybind.
    (.addKeyListener tree (proxy [KeyListener] []
                            (keyPressed [e]
                                        (info (. e keyCode))
                                        (case (. e keyCode)
                                              ((SWT/CR)) (info "enter")
                                              "default"))
                            (keyReleased [e])))

    ;history
    (register-menu-items (history-get) tree)
    ;templates
    (let [template (TreeItem. tree SWT/NULL)]
      (.setText template "Registered Templates")
      (register-menu-items (templates-get) template))
;    ;exit
;    (let [menu-item (MenuItem. popup-menu SWT/PUSH)]
;      (.setText menu-item "Exit")
;      (register-menu-item [menu-item]
;                          (.close shell)))
;    (.setVisible popup-menu true)
    (info "display-menu end")
    (doto shell
      (.pack)
      (.open))
    (show-center shell)
    shell))

;(defn key-bind [key mapto]
;  (let [event (Event.)]
;    (Thread/sleep 100)
;    (set! (. event keyCode) (bit-or (Character/digit SWT/ALT 10) 121)) ; 121 is 'y'.
;    (set! (. event type) SWT/KeyDown)
;    (.post display event)
;    (set! (. event type) SWT/KeyUp)
;    (.post display event)
;  )

(defn tasktray-register []

  (let [display (Display.)
        shell (Shell. display)
        tray (.getSystemTray display)
        tray-item (TrayItem. tray SWT/NONE)]
    (.setText shell "CLCLCL")
    ;listen server start
    (start-server (fn [in out]
                      (.syncExec display
                                  (proxy [Runnable] []
                                    (run []
                                         (.pack shell)
                                         (show-center shell)
                                         (let [alert (MessageBox. shell SWT/YES)]
                                           (.setMessage alert "Please push enter.")
                                           (.open alert))
                                         (.setVisible shell false)
                                           (let [compo (Composite. shell SWT/BORDER)
                                                 menu (do
                                                        (display-menu shell))]
                                             (.setMenu compo menu)
                                             (.setVisible menu true)
                                             (.setVisible compo true)))))
                    (let [*out* (PrintWriter. out)]
                      (println "ok")
                      (flush)
                      (.close *out*))))

    (doto tray-item
      (.setToolTipText "clclcl")
      (.setImage (Image. display (get-icon-image-stream)))
      (.addListener SWT/Selection (proxy [Listener] []
                                    (handleEvent [e]
                                                 (display-menu shell))))
      (.addListener SWT/MenuDetect (proxy [Listener] []
                                     (handleEvent [e]
                                                  (display-menu shell)))))

    ; main event loop
    (loop []
      (if-not  (.isDisposed shell)
        (do
          (if (.readAndDispatch display)
          (.sleep display))
          (recur))))
    ; clean up.
    (info "clean up.")
    (stop-server)
    (.dispose display)
    (.dispose shell)))
