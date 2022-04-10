(ns popcon.plot
  (:gen-class)
  (:require [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]))

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
    (println "average period   :" period)))
