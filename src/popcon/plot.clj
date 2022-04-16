(ns popcon.plot
  (:gen-class)
  (:require [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]])
  (:import java.net.URLEncoder))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; reading data

(defn popcon-url [ref-name pkg-names]
  (let [enc (fn [s] (URLEncoder/encode s "UTF-8"))]
    (str "https://qa.debian.org/cgi-bin/popcon-data?packages="
         (enc ref-name) "+" (string/join "+" (map enc pkg-names)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; prepare data set

(defn prepare-data-set [ref-name pkg-names]
  (let [url (popcon-url ref-name pkg-names)]
    (println "data url         :" url)))

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
    (prepare-data-set ref-name pkg-names)))
