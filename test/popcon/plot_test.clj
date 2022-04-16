(ns popcon.plot-test
  (:require [clojure.test :refer :all]
            [popcon.plot :as T]))

(deftest popcon-url-test
  (testing "url for popcon data")
  (is (= "https://qa.debian.org/cgi-bin/popcon-data?packages=pkg1+pkg2"
         (T/popcon-url "pkg1" '("pkg2"))))
  (is (= "https://qa.debian.org/cgi-bin/popcon-data?packages=pkg1+g%2B%2B"
         (T/popcon-url "pkg1" '("g++"))))
  (is (= "https://qa.debian.org/cgi-bin/popcon-data?packages=g%2B%2B+pkg2"
         (T/popcon-url "g++" '("pkg2"))))
  (is (= "https://qa.debian.org/cgi-bin/popcon-data?packages=pkg1+pkg2+pkg3"
         (T/popcon-url "pkg1" '("pkg2" "pkg3"))))
  (is (= "https://qa.debian.org/cgi-bin/popcon-data?packages=pkg1+g%2B%2B+pkg3"
         (T/popcon-url "pkg1" '("g++" "pkg3"))))
  (is (= "https://qa.debian.org/cgi-bin/popcon-data?packages=g%2B%2B+pkg2+pkg3"
         (T/popcon-url "g++" '("pkg2" "pkg3")))))

(deftest command-line-interface
  (testing "cli arguments and options")
  (is (= ["pkg1" "pkg2"]
         (:arguments (T/parse-cli ["pkg1" "pkg2"]))))
  (is (= {:average "m", :reference "base-files"}
         (:options (T/parse-cli ["pkg1" "pkg2"]))))
  (is (= {:average "q", :reference "dpkg"}
         (:options (T/parse-cli ["-a q" "-r dpkg"])))))
