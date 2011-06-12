(defproject clclcl "0.2.3-beta"
            :description "access to clipboard histories."
            :dependencies [[org.clojure/clojure "1.2.0"]
                           [org.clojure/clojure-contrib "1.2.0"]
                           [org.eclipse/swt-gtk-linux-x86_64 "3.5.2"]     ;linux 64bitOS
                           ;[org.eclipse/swt-gtk-linux-x86 "3.5.2"]       ;linux 32bitOS
                           ;[org.eclipse/swt-cocoa-macosx-x86_64 "3.5.2"] ;macosx 64bitOS
                           ;[org.eclipse/swt-cocoa-macosx "3.5.2"]        ;macosx 32bitOS
                           ;[org.eclipse/swt-carbon-macosx "3.5.2"]       ;macosx
                           ;[org.eclipse/swt-win32-win32-x86_64 "3.5.2"]  ;windows 64bitOS
                           ;[org.eclipse/swt-win32-win32-x86 "3.5.2"]     ;windows 32bitOS
                           [sqlitejdbc/sqlitejdbc "0.5.6"]]
            :main clclcl.core)
