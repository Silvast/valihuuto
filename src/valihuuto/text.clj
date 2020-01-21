(ns valihuuto.text
  (:require [clojure.string :as str]
            [feedme :as feedme]
            [pdfboxing.text :as text]
            [clj-http.client :as client]
            [clojure.java.io :as io]
            [valihuuto.config :refer [config]]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [clj-time.format :as f]
            [clojure.string :as str]
            [valihuuto.db.db :as db]
            [clojure.tools.logging :as log])
  (:gen-class))

(def redundant-calls ["Puhemies koputtaa" "Puhe-\nmies koputtaa"
                      "Pu-\nhemies koputtaa" "Hälinää"
                      "Välihuutoja" "Keskustelu asiasta"
                      "Naurua" "Vastauspuheenvuoropyyntöjä"
                      "Pöytäkirja PTK"
                      "/2019"
                      "PTK"])

(defn is-redunant? [huuto]
  (some true? (map #(or (str/starts-with? huuto %) (str/includes? huuto %))
                   redundant-calls)))

(defn trim-text [text]
  (-> text
      (clojure.string/replace "[" "")
      (clojure.string/replace "]" "")
      (clojure.string/replace "\n" " ")
      (clojure.string/replace "-\n" " ")
      (clojure.string/replace "- " "")))

(defn find-valihuudot [file]
  (->> (text/extract file)
       (re-seq #"\[.*?\]")
       (map #(trim-text %))
       (distinct)
       (remove #(is-redunant? %))))

(defn download-pdf [uri file]
  (with-open [in (io/input-stream uri)
              out (io/output-stream file)]
    (io/copy in out)))

(defn get-huudettu-date
  "Finds and formats date from entry"
  [entry]
  (let [huudettu
        (second (str/split (:content entry) #"\s+"))
        custom-formatter (f/formatter "dd.MM.yyyy")]
    (f/parse custom-formatter huudettu)))

(defn get-valihuudot [title]
  (let [split-name (str/split title #"\s+")
        filename (format "%s_%s.pdf"
                         (first split-name)
                         (str/replace (second split-name) #"\/" "+"))
        download-url
        (format "%s/%s" "https://www.eduskunta.fi/FI/vaski/Poytakirja/Documents"
                filename)
        file (format "/%s/%s" "tmp" filename)]
    (if (nil? (download-pdf download-url file))
      {:valihuudot (find-valihuudot file)
       :memo-url download-url})))