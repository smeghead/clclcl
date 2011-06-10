(ns clclcl.tasktray
  (:gen-class)
  (:use clojure.contrib.logging clojure.test clclcl.options clclcl.database clclcl.clipboard clclcl.history clclcl.utils clclcl.templates clclcl.server)
  (:import 
     (java.io PrintWriter)
     (org.eclipse.swt SWT)
     (org.eclipse.swt.widgets Event Display Tray TrayItem Shell Listener Composite MessageBox ToolTip Tree TreeItem)
     (org.eclipse.swt.graphics Device Image)
     (org.eclipse.swt.events KeyListener ShellAdapter MouseListener)
     (javax.imageio ImageIO)))
(impl-get-log (str *ns*))

(defn get-icon-image-stream []
  (.getResourceAsStream (.getClass "") "/clclcl/clclcl.png"))

(defn key-post [shell key map-to]
  (let [event (Event.)]
    (set! (. event keyCode) key)
    (set! (. event type) map-to)
    (.post (.getDisplay shell) event)))

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

(defn register-menu-items [entries tree registerd-items]
  (loop [items entries
         acc registerd-items]
    (if-not (empty? items)
      (let [item (first items) 
            loaded-item (cond
                            (coll? item) item
                            :else {:name item :data item})
            name (loaded-item :name)
            data (loaded-item :data)
            item (TreeItem. tree SWT/NULL)]
        (.setText item (format-entry-for-item name))
;        (if-not (= (.getText menu-item) (str data))
;          (.setToolTipText menu-item (str data)))
;        (register-menu-item [tree]
;                            (clipboard-set (if (list? data)
;                                             ((eval data)) ;eval form was written in templates.clj
;                                             data)))
        (recur (rest items) (assoc acc (. item hashCode) (fn []
                                                           (clipboard-set (if (list? data)
                                                                            ((eval data)) ;eval form was written in templates.clj
                                                                            data))))))
        acc)))

(def *registerd-items* (ref {}))

(defn select-selection [shell item]
  (let [fn (get @*registerd-items* (. item hashCode))]
    (info fn)
    (if-not (nil? fn)
      (fn)); execute stored function
    (info "enter")
    (.setVisible shell false)))

(defn display-menu [shell tree]
  (.removeAll tree)
  ;history
  (dosync (ref-set *registerd-items* (register-menu-items (history-get) tree {})))
  ;templates
  (let [template (TreeItem. tree SWT/NULL)]
    (.setText template "Registered Templates")
    (dosync (ref-set *registerd-items* (register-menu-items (templates-get) template @*registerd-items*))))
  ;    ;exit
  ;    (let [menu-item (MenuItem. popup-menu SWT/PUSH)]
  ;      (.setText menu-item "Exit")
  ;      (register-menu-item [menu-item]
  ;                          (.close shell)))
  ;    (.setVisible popup-menu true)
  (.pack shell)
  (.setSelection tree (aget (.getItems tree) 0)) ; select first element.
  (show-center shell)
  shell)

(defn tasktray-register []
  (let [display (Display.)
        shell (Shell. display)
        tray (.getSystemTray display)
        tray-item (TrayItem. tray SWT/NONE)
        tree (Tree. shell (bit-or SWT/BORDER SWT/V_SCROLL))
        client-area (.getClientArea shell)]
    (doto shell
      (.setText "CLCLCL")
      (.addShellListener (proxy [ShellAdapter] []
                           (shellClosed [e]
                                        (.setVisible shell false)
                                        (set! (. e doit) false)))))
    (.setBounds tree (. client-area x) (. client-area y) 500 600)

    ;register mouse action.
    (.addMouseListener tree (proxy [MouseListener] []
                              (mouseDown [e])
                              (mouseUp [e])
                              (mouseDoubleClick [e]
                                                (let [item (aget (.getSelection tree) 0)]
                                                  (select-selection shell item)))))

    ;register keybind.
    (.addKeyListener tree (proxy [KeyListener] []
                            (keyPressed [e]
                                        (info (. e keyCode))
                                        (let [code (. e keyCode)
                                              char (. e character)
                                              item (aget (.getSelection tree) 0)]
                                          (cond
                                            (= char SWT/CR) (select-selection shell item)
                                            (= char SWT/ESC) (do
                                                               (.setVisible shell false)
                                                               (set! (. e doit) false))
                                            (= code SWT/ARROW_DOWN) (info "down")
                                            (= code SWT/ARROW_UP) (info "up")
                                            (= code 106) (do ; j
                                                           (key-post shell SWT/ARROW_DOWN SWT/KeyDown)
                                                           (set! (. e doit) false))
                                            (= code 107) (do ; k
                                                           (key-post shell SWT/ARROW_UP SWT/KeyDown)
                                                           (set! (. e doit) false))
                                            (= code 104) (do ; h
                                                           (.setExpanded item false)
                                                           (set! (. e doit) false))
                                            (= code 108) (do ; l
                                                           (.setExpanded item true)
                                                           (set! (. e doit) false))
                                            :else (set! (. e doit) false))))
                            (keyReleased [e]
                                         (let [code (. e keyCode)
                                               char (. e character)
                                               item (aget (.getSelection tree) 0)]
                                           (cond
                                             (= char SWT/CR) (info "enter")
                                             (= code SWT/ARROW_DOWN) (info "down")
                                             (= code SWT/ARROW_UP) (info "up")
                                             (= code 106) (do ; j
                                                            (key-post shell SWT/ARROW_DOWN SWT/KeyUp)
                                                            (set! (. e doit) false))
                                             (= code 107) (do ; k
                                                            (key-post shell SWT/ARROW_UP SWT/KeyUp)
                                                            (set! (. e doit) false))
                                             (= code 104) (do ; h
                                                            (set! (. e doit) false))
                                             (= code 108) (do ; l
                                                            (set! (. e doit) false))
                                             :else (set! (. e doit) false))))))

    ;listen server start
    (start-server (fn [in out]
                    (.syncExec display
                               (proxy [Runnable] []
                                 (run []
                                      (display-menu shell tree))))
                    (let [*out* (PrintWriter. out)]
                      (println "ok")
                      (flush)
                      (.close *out*))))

    (doto tray-item
      (.setToolTipText "clclcl")
      (.setImage (Image. display (get-icon-image-stream)))
      (.addListener SWT/Selection (proxy [Listener] []
                                    (handleEvent [e]
                                                 (display-menu shell tree))))
      (.addListener SWT/MenuDetect (proxy [Listener] []
                                     (handleEvent [e]
                                                  (display-menu shell tree)))))

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
