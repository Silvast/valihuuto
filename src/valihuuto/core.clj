(ns valihuuto.core
  (:require [clojure.string :as str]
            [feedme :as feedme]
            [pdfboxing.text :as text]
            [clj-http.client :as client]
            [clojure.java.io :as io]
            [valihuuto.config :refer [config]]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [clj-time.format :as f]
            [valihuuto.db.db :as db]
            [clojure.tools.logging :as log]
            [valihuuto.db.migrations :as m]
            [valihuuto.tweeting :as tweeting])
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
      (remove #(str/starts-with? % "Puhemies")
              (find-valihuuto #"(?<=\[)(.*?)(?=\])" (text/extract file))))))

(defn get-huudettu-date
  "Finds and formats date from entry"
  [entry]
  (let [huudettu
        (second (str/split (:content entry) #"\s+"))
        custom-formatter (f/formatter "dd.MM.yyyy")]
    (f/parse custom-formatter huudettu)))

(defn get-last-from-rss
  "This is called when db is empty and nothing has been tweeted yet"
  []
  (let [feed (feedme/parse (:rss-url config))
        entry (last (:entries feed))
        title (:title entry)
        info {:huudettu (get-huudettu-date entry)
              :memo-version
              (first (re-find #"([^/]+)"
                              (second (str/split title #"\s+"))))}]
    (tweeting/tweet (get-valihuudot title) info)))

(defn get-from-rss-by-version
  "Checks the state of tweets from db and if new memos exists, tweets from them"
  [latest]
  (let [year
        (t/year (c/from-sql-date (:viimeisin-twiitattu-pvm
                                  latest)))
        feed (feedme/parse (:rss-url config))
        memo-name (format "%s%s/%s %s" "PTK " (inc (:versio latest)) year "vp")
        filtered-match (filter #(= (:title %) memo-name) (:entries feed))
        huudettu (get-huudettu-date (first filtered-match))
        info {:huudettu huudettu :memo-version (str (inc (:versio latest)))}]
    (if (some? filtered-match)
      (do
        (log/info "Found new huutos, tweeting them now.")
        (tweeting/tweet
         (get-valihuudot (:title (first filtered-match))) info)))))

(defn -main
  "Will check the situation with the tweets and tweet if suitable."
  [& args]
  (let [latest (db/get-last-tweeted)]
    (log/info "Running migrations")
    (m/migrate!)
    (log/info "Migrations done.")
    (log/info "Fetching tweets")
    (if (nil? latest)
      (get-last-from-rss)
      (get-from-rss-by-version latest))))