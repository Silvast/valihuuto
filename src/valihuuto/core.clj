(ns valihuuto.core
  (:require [clojure.string :as str]
            [feedme :as feedme]
            [pdfboxing.text :as text]
            [clj-http.client :as client]
            [clojure.java.io :as io])
  (:gen-class))

(defn find-valihuuto [re text]
  (let [matcher (re-matcher re text)]
    (loop [match (first (re-find matcher))
           result []]
      (if-not match
        result
        (recur (first (re-find matcher))
               (conj result match))))))

(defn download-pdf [uri file]
  (with-open [in (io/input-stream uri)
              out (io/output-stream file)]
    (io/copy in out)))

(def rss-url
  "https://www.eduskunta.fi/_layouts/15/feed.aspx?xsl=1&web=%2FFI%2Frss%2Dfeeds&page=8192fae7-172f-46ba-8605-75c1e750007a&wp=3527e156-7a72-443c-8698-9b5596317471&pageurl=%2FFI%2Frss%2Dfeeds%2FSivut%2FTaysistuntojen%2Dpoytakirjat%2DRSS%2Easpx")

(defn get-filename []
  (->> (feedme/parse rss-url)
       (#(nth (:entries %) 1))
       (#(str/split (:title %) #"\s+"))
       (#(format "%s_%s.pdf" (first %) (str/replace (second %) #"\/"
                                                    "+")))))

(defn get-latest-valihuudot []
  (let [filename (get-filename)
        download-url (format "%s/%s" "https://www.eduskunta.fi/FI/vaski/Poytakirja/Documents"
                             filename)
        file (format "/%s/%s" "tmp" filename)]
    (if (nil? (download-pdf download-url file))
      (remove #(str/starts-with? % "Puhemies")
              (find-valihuuto #"(?<=\[)(.*?)(?=\])" (text/extract file))))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println (get-latest-valihuudot)


;;
))



