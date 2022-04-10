(ns popcon.plot-test
  (:require [clojure.test :refer :all]
            [popcon.plot :as T]))

(deftest command-line-interface
  (testing "cli arguments and options")
  (is (= ["pkg1" "pkg2"]
         (:arguments (T/parse-cli ["pkg1" "pkg2"]))))
  (is (= {:average "m", :reference "base-files"}
         (:options (T/parse-cli ["pkg1" "pkg2"]))))
  (is (= {:average "q", :reference "dpkg"}
         (:options (T/parse-cli ["-a q" "-r dpkg"])))))
