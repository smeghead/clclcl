(ns clclcl.options-test
  (:use [clclcl.options] :reload-all)
  (:use [clojure.test]))

; setting file for test.
(def *test-setting-file* "resources/test-init-file")

(defn if-exists-and-delete [file-name]
  (let [file (java.io.File. file-name)]
    (if (.exists file)
      (.delete file))))

(use-fixtures :each (fn [f]
                     (if-exists-and-delete *test-setting-file*)
                     (f)
                     (if-exists-and-delete *test-setting-file*)))

(deftest get-options-normal
         (binding [*user-setting-file* *test-setting-file*]
           (let [options (get-options)]
             (testing "In setting-file, key exists check."
                      (is (contains? options :list-max) ":list-max")
                      (is (contains? options :font-name) ":font-name")
                      (is (contains? options :font-size) ":font-size")
                      (is (contains? options :watch-interval) ":watch-interval")
                      (is (contains? options :server-port) ":server-port")))))

