(ns valihuuto.huudot
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
            [java-time :as jt]
            [valihuuto.db.migrations :as m]
            [valihuuto.tweeting :as tweeting]
            [clojure.tools.logging :as log]
            [valihuuto.text :as text-handling]))


(def istuntokausi [{:year 2020 :kausi (jt/interval (jt/offset-date-time 2020 2 04)
                                                   (jt/offset-date-time 2020 6
                                                                        30 23
                                                                        59))
                    :part 1}
                   {:year 2020 :kausi (jt/interval (jt/offset-date-time 2020 9 01)
                                                   (jt/offset-date-time 2020 12 22))
                    :part 2}
                   {:year 2021 :kausi (jt/interval (jt/offset-date-time 2021 2 02)
                                                   (jt/offset-date-time 2021 6
                                                                        30 23
                                                                        59))
                    :part 1}
                   {:year 2021 :kausi (jt/interval (jt/offset-date-time 2021 9 01)
                                                   (jt/offset-date-time 2021
                                                                        12 22 23
                                                                        59))
                    :part 2}
                   {:year 2022 :kausi (jt/interval (jt/offset-date-time 2022 2 04)
                                                   (jt/offset-date-time 2022 6
                                                                        30 23
                                                                        59))
                    :part 1}
                   {:year 2022 :kausi (jt/interval (jt/offset-date-time 2022 9 01)
                                                   (jt/offset-date-time 2022
                                                                        12 22 23
                                                                        59))
                    :part 2}
                   {:year 2023 :kausi (jt/interval (jt/offset-date-time 2023 2 04)
                                                   (jt/offset-date-time 2023 6
                                                                        30 23
                                                                        59))
                    :part 1}
                   {:year 2023 :kausi (jt/interval (jt/offset-date-time 2023 9 01)
                                                   (jt/offset-date-time 2023
                                                                        12 22 23
                                                                        59))
                    :part 2}
                   {:year 2024 :kausi (jt/interval (jt/offset-date-time 2024 2 04)
                                                   (jt/offset-date-time 2024 6
                                                                        30 23
                                                                        59))
                    :part 1}
                   {:year 2024 :kausi (jt/interval (jt/offset-date-time 2024 9 01)
                                                   (jt/offset-date-time 2024
                                                                        12 22 23
                                                                        59))
                    :part 2}])

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

(defn make-tweets [filtered-match latest-versio]
  (log/info "Make new tweets")
  (let [huudettu (text-handling/get-huudettu-date (first filtered-match))
        info {:huudettu huudettu :memo-version (str (inc latest-versio))}
        valihuudot (text-handling/get-valihuudot (:title (first
                                                          filtered-match)))]
    (do
      (log/info "Found new huutos, tweeting them now.")
      (tweeting/tweet (:valihuudot valihuudot)
                      (assoc info :memo-url (:memo-url valihuudot))))))

(defn get-from-rss-by-version
  "Checks the state of tweets from db and if new memos exists, tweets from them"
  [latest-versio year]
  (let [feed (feedme/parse (:rss-url config))
        memo-start (format "%s%s/%s%s" "PTK " (inc latest-versio) year " vp")
        memo-start2 (format "%s%s/%s%s" "PTK " (inc latest-versio) (dec year) " vp")
        filtered-match (filter #(= (:title %) memo-start) (:entries feed))
        filtered-match2 (filter #(= (:title %) memo-start2) (:entries feed))]
    (cond
      (some? (not-empty filtered-match)) (make-tweets filtered-match latest-versio)
      (some? (not-empty filtered-match2)) (make-tweets filtered-match2 latest-versio)
      :else (log/info "No new tweets"))))

(defn is-istuntokausi? [day]
  (let [kaudet (filter #(= (:year %) (jt/as day :year)) istuntokausi)
        first-interval (:kausi (first kaudet))
        second-interval (:kausi (second kaudet))]
    (or
     (jt/contains? first-interval day)
     (jt/contains? second-interval day))))

(defn get-previous-kausi [day]
  (->> istuntokausi
       (filter #(jt/before? (jt/end (:kausi %)) (jt/instant day)))
       (last)))

(defn get-huudot-from-last-kausi [day]
  (let [previous-kausi (get-previous-kausi day)
        last-tweeted-versio (:versio (db/get-istuntotauko-tweeted
                                      previous-kausi))]
    (if (some? last-tweeted-versio)
      (tweeting/tweet-old-from-versio previous-kausi
                                      (Integer. last-tweeted-versio))
      (tweeting/tweet-old-from-versio previous-kausi (db/get-first-versio
                                                      previous-kausi)))))


(defn search-huudot []
  (let [today  (jt/offset-date-time)
        latest (db/get-last-tweeted)
        istuntokausi (is-istuntokausi? (jt/minus today (jt/days 1)))]
    (cond
      (= true istuntokausi) (get-from-rss-by-version (or (:versio latest) 1) (jt/as today :year))
      (= false istuntokausi) (get-huudot-from-last-kausi today))))