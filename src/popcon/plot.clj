(ns popcon.plot
  (:gen-class)
  (:require [clojure.data.json :as json]
            [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]])
  (:import java.net.URLEncoder))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; reading data

(defn popcon-url [ref-name pkg-names]
  (let [enc #(URLEncoder/encode % "UTF-8")]
    (str "https://qa.debian.org/cgi-bin/popcon-data?packages="
         (enc ref-name) "+" (string/join "+" (map enc pkg-names)))))

(defn read-data [url]
  (json/read-str (slurp url)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; prepare data set

(defn collect-installations [package-name popcon-data]
  [package-name
   (map (juxt key (comp #(reduce + (vals %)) val)) (popcon-data package-name))])

(defn compute-relative [ref-data [pkg-name pkg-data]]
  (let [r (into {} ref-data)
        p (into {} pkg-data)
        x (select-keys p (keys r))]
    [pkg-name
     (map (juxt first #(double (/ (second %) (r (first %))))) x)]))

(defn prepare-data-set [ref-name pkg-names]
  (let [url (popcon-url ref-name pkg-names)]
    (println "data url         :" url)
    (let [data (read-data url)
          ref-data (second (collect-installations ref-name data))
          pkg-data-set (map #(collect-installations % data) pkg-names)
          rel-data-set (map #(compute-relative ref-data %) pkg-data-set)]
      rel-data-set)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; output

(defn print-recent-data [data-set N]
  (doseq [[name data] data-set]
    (println (str "=== " name " ==="))
    (doseq [[d v] (take (min N (count data)) data)]
      (println (format "%s\t%.2e" d v)))))

(defn print-and-write [ref-name pkg-names]
  (let [data-set (prepare-data-set ref-name pkg-names)]
    (println "recent data:")
    (print-recent-data data-set 10)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; command line interface

(defn parse-cli [args]
  (let [cli-options [["-a" "--average PERIOD" "average period (d|m|q|y)"
                      :default "m" :parse-fn string/trim]
                     ["-r" "--reference NAME" "reference package name"
                      :default "base-files" :parse-fn string/trim]]]
    (parse-opts args cli-options)))

(defn -main [& args]
  (let [cli-args (parse-cli args)
        period (:average (:options cli-args))
        ref-name (:reference (:options cli-args))
        pkg-names (:arguments cli-args)]
    (println "reference package:" ref-name)
    (println "packages         :" (string/join " " pkg-names))
    (println "average period   :" period)
    (print-and-write ref-name pkg-names)))
