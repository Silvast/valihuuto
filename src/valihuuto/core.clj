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
            [valihuuto.tweeting :as tweeting]
            [valihuuto.text :as text-handling])
  (:gen-class))

(defn get-last-from-rss
  "This is called when db is empty and nothing has been tweeted yet"
  []
  (let [feed (feedme/parse (:rss-url config))
        entry (last (:entries feed))
        title (:title entry)
        info {:huudettu (text-handling/get-huudettu-date entry)
              :memo-version
              (first (re-find #"([^/]+)"
                              (second (str/split title #"\s+"))))}
        valihuudot (text-handling/get-valihuudot title)]
    (tweeting/tweet (:valihuudot valihuudot)
                    (assoc info :memo-url (:memo-url valihuudot)))))

(defn get-from-rss-by-version
  "Checks the state of tweets from db and if new memos exists, tweets from them"
  [latest-versio year]
  (let [feed (feedme/parse (:rss-url config))
        memo-start (format "%s%s/%s%s" "PTK " (inc latest-versio) year " vp")
        filtered-match (filter #(= (:title %) memo-start) (:entries feed))]
    (if (not-empty filtered-match)
      (let [huudettu (text-handling/get-huudettu-date (first filtered-match))
            info {:huudettu huudettu :memo-version (str (inc latest-versio))}
            valihuudot (text-handling/get-valihuudot (:title (first
                                                              filtered-match)))]
        (do
          (log/info "Found new huutos, tweeting them now.")
          (tweeting/tweet (:valihuudot valihuudot)
                          (assoc info :memo-url (:memo-url valihuudot)))))
      (log/info "No new tweets"))))


(defn -main
  "Will check the situation with the tweets and tweet if suitable."
  [& args]
  (let [latest (db/get-last-tweeted)]
    (log/info "Running migrations")
    (m/migrate!)
    (log/info "Migrations done.")
    (log/info "Fetching tweets")
    (cond
      (nil? latest) (get-last-from-rss)
      (= (t/year (t/now)) (t/year (c/from-sql-date
                                    (:viimeisin-twiitattu-pvm
                                      latest))))
      (get-from-rss-by-version (:versio latest) (:year config))
      :else (get-from-rss-by-version 0 (:year config)))))

