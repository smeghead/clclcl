(ns clclcl.tasktray
  (:gen-class)
  (:use clojure.contrib.logging clojure.test clclcl.options clclcl.database clclcl.clipboard clclcl.history clclcl.utils clclcl.templates)
  (:import 
     (org.eclipse.swt SWT)
     (org.eclipse.swt.widgets Display Tray TrayItem Shell Menu MenuItem Listener)
     (org.eclipse.swt.graphics Device Image)
     (org.eclipse.swt.events SelectionAdapter)
     (javax.imageio ImageIO)))
(impl-get-log (str *ns*))

(defn get-icon-image-stream []
  (.getResourceAsStream (.getClass "") "/clclcl/clclcl.png"))

(defn format-entry-for-item [entry]
  (let [ent (.replace entry "\n" "")
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
;      (.addListener popup-menu SWT/KeyDown (proxy [Listener] []
;                                                (handleEvent [e]
;                                                            (info "pressed."))))
      ;history
      (register-menu-items (history-get) popup-menu)
      (MenuItem. popup-menu SWT/SEPARATOR)
      ;templates
      (let [template-menu-item (MenuItem. popup-menu SWT/CASCADE)
            sub-menu (Menu. popup-menu)]
        (doto template-menu-item
          (.setText "Registered Templates")
          (.setMenu sub-menu))
        (register-menu-item [template-menu-item])
        (register-menu-items (templates-get) sub-menu))
      (MenuItem. popup-menu SWT/SEPARATOR)
      ;exit
      (let [menu-item (MenuItem. popup-menu SWT/PUSH)]
        (.setText menu-item "Exit")
        (register-menu-item [menu-item]
                            (java.lang.System/exit 0)))
      (.setVisible popup-menu true))))

(defn tasktray-register []
  (let [display (Display.)
        shell (Shell. display)
        tray (.getSystemTray display)
        tray-item (TrayItem. tray SWT/NONE)]
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
      (if (.readAndDispatch display)
        (do
          (.sleep display)
          (recur))
        (.dispose display)))
    ; clean up.
    (.dispose display)
    (.dispose shell)))
