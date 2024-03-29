(ns popcon.plot
  (:gen-class)
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]
            [oz.core :as oz])
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
;; collect installations

(defn collect-installations [package-name popcon-data]
  [package-name
   (map (juxt key (comp #(reduce + (vals %)) val)) (popcon-data package-name))])

(defn compute-relative [ref-data [pkg-name pkg-data]]
  (let [r (into {} ref-data)
        p (into {} pkg-data)
        x (select-keys p (keys r))]
    [pkg-name
     (map (juxt first #(double (/ (second %) (r (first %))))) x)]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; average over a period

(defn map-to-month [date]
  (string/replace date #"-\d{2}$" "-15"))

(defn map-to-quarter [date]
  (-> date
      (string/replace ,, #"-(01|02|03)-\d{2}$" "-02-15")
      (string/replace ,, #"-(04|05|06)-\d{2}$" "-05-15")
      (string/replace ,, #"-(07|08|09)-\d{2}$" "-08-15")
      (string/replace ,, #"-(10|11|12)-\d{2}$" "-11-15")))

(defn map-to-year [date]
  (string/replace date #"-\d{2}-\d{2}$" "-06-01"))

(defn map-to-period [p date]
  (cond
    (= p "m") (map-to-month date)
    (= p "q") (map-to-quarter date)
    (= p "y") (map-to-year date)
    :else date))

(defn average [period [pkg-name pkg-data]]
  [(str pkg-name "[" period "]")
   (->> pkg-data
        (map (juxt #(map-to-period period (first %)) second) ,,)
        (group-by first ,,)
        (map (juxt first
                   (fn [x] (#(/ (reduce + %) (count %)) (map second (second x)))))
             ,,)
        (sort ,,)
        (reverse ,,))])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; prepare data set

(defn prepare-data-set [period ref-name pkg-names]
  (let [url (popcon-url ref-name pkg-names)
        data (read-data url)
        ref-data (second (collect-installations ref-name data))]
    (println "data url         :" url)
    (->> pkg-names
         (map #(collect-installations % data) ,,)
         (map #(compute-relative ref-data %) ,,)
         (map #(average period %) ,,))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; output

(defn print-recent-data [data-set N]
  (doseq [[name data] data-set]
    (println (str "=== " name " ==="))
    (doseq [[d v] (take (min N (count data)) data)]
      (println (format "%s\t%.2e" d v)))))

(defn write-data [[ref-name data-set]]
  (doseq [[pkg-name pkg-data] data-set]
    (let [file-name (str ref-name "_" pkg-name ".txt")]
      (println file-name)
      (with-open [wrtr (io/writer file-name)]
        (doseq [[d v] pkg-data]
          (.write wrtr (format "%s\t%.3e\n" d v)))))))

(defn plot-description [[ref-name data-set]]
  (let [plot-data (fn [[pkg-name pkg-data]]
                    (map (fn [[d v]] {:date d :value v :package pkg-name}) pkg-data))
        line-layer {:mark {:type "line" :interpolate "natural"}
                    :encoding {:x {:field "date"
                                   :type "temporal"
                                   :title nil}
                               :y {:field "value"
                                   :type "quantitative"
                                   :title (str "package / " ref-name)
                                   :scale {:zero false}}
                               :color {:field "package" :type "nominal"}}}
        point-layer (assoc line-layer :mark {:type "circle" :size 100})]
    {:data {:values (flatten (map plot-data data-set))}
     :width 1200 :height 800
     :config {:legend {:labelFontSize 20 :titleFontSize 20}
              :axis {:labelFontSize 20 :titleFontSize 20}}
     :layer (list line-layer point-layer)}))

(defn plot-and-write [period ref-name pkg-names]
  (let [data-set (prepare-data-set period ref-name pkg-names)]
    (println "recent data:")
    (print-recent-data data-set 10)
    (oz/view! (plot-description [ref-name data-set]))
    (println "writing data:")
    (write-data [ref-name data-set])))

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
    (plot-and-write period ref-name pkg-names)))
