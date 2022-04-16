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

(def json-data (T/read-data "resources/popcon-data.json"))
(def pkg1-data (T/collect-installations "pkg1" json-data))
(def pkg2-data (T/collect-installations "pkg2" json-data))

(deftest collect-installations-test
  (testing "total number of intallations"
    (is (= "pkg1" (first pkg1-data)))
    (is (= '(["2022-03-01" 12]
             ["2022-03-02" 11]
             ["2022-03-03" 13]
             ["2022-03-04" 14]
             ["2022-04-01" 10]
             ["2022-04-02" 11]
             ["2022-04-03" 12]
             ["2022-04-04" 14]
             ["2022-04-05" 16])
           (sort (second pkg1-data))))
    (is (= "pkg2" (first pkg2-data)))
    (is (= '(["2022-03-01" 100]
             ["2022-03-02" 100]
             ["2022-03-03" 100]
             ["2022-03-04" 100]
             ["2022-04-01" 100]
             ["2022-04-02" 200]
             ["2022-04-03" 300]
             ["2022-04-04" 400]
             ["2022-04-06" 400])
           (sort (second pkg2-data))))))

(deftest command-line-interface-test
  (testing "cli arguments and options")
  (is (= ["pkg1" "pkg2"]
         (:arguments (T/parse-cli ["pkg1" "pkg2"]))))
  (is (= {:average "m", :reference "base-files"}
         (:options (T/parse-cli ["pkg1" "pkg2"]))))
  (is (= {:average "q", :reference "dpkg"}
         (:options (T/parse-cli ["-a q" "-r dpkg"])))))
