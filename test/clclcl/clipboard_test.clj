(ns clclcl.clipboard-test
  (:use [clclcl.clipboard] :reload-all)
  (:use [clojure.test]))

(deftest get-and-set-clipboard
         (doall (map
                  #(let [v %]
                     (clipboard-set v)
                     (is (= v (clipboard-get)) "should be same got data and set data."))
                  '("aaa" "bbb" "ccc"))))
