(ns valihuuto.tweeting
  (:require [twitter.oauth :as oauth]
            [twitter.api.restful :as rest]
            [valihuuto.db.db :as db]
            [clojure.tools.logging :as log]
            [clj-time.coerce :as c]
            [environ.core :refer [env]]))

(defonce app-consumer-key (env :twitter-consumer-key))
(defonce app-consumer-secret (env :twitter-consumer-secret))
(defonce user-access-token (env :twitter-access-token))
(defonce user-access-token-secret (env :twitter-access-token-secret))

(def creds (oauth/make-oauth-creds
            app-consumer-key
            app-consumer-secret
            user-access-token
            user-access-token-secret))

(defn save-valihuudot! [valihuudot info]
  (doseq [msg valihuudot]
    (try
        (db/save-tweeted-valihuuto! msg (:huudettu info) (:memo-version info))
      (catch Exception e
        (log/warn "Could not save the valihuuto: " e)))))

(defn trim-text [text]
  (-> text
      (clojure.string/replace "\n" " ")
      (clojure.string/replace "-\n" " ")
      (clojure.string/replace "- " "")))

(defn tweet-test [valihuudot info]
  (if (> (count valihuudot) 0)
    (rest/statuses-update :oauth-creds creds :params
                                  {:status
                                   (str "Twiittaan välihuudot pöytäkirjasta: "
                                        (:memo-url info))})

    (rest/statuses-update :oauth-creds creds :params
                          {:status
                           (str "Valitettavasti pöytäkirjassa: "
                                (:memo-url info) " - ei ollut yhtään
                                välihuutoa.")}))

  (db/save-tweeted-tila! (:huudettu info) (:memo-version info))
  (let [amount (count valihuudot)
        pause-duration (int (/ 36000000 (max amount 1)))
        huudot (distinct (map #(trim-text %) valihuudot))]
    (doseq [msg huudot]
      (log/info "Now tweeting: " msg)
      (log/info "info: " info)
      (try
        (rest/statuses-update :oauth-creds creds :params
                                                   {:status msg})
        (catch Exception e
          (log/warn "Could not send a tweet, countered error: " e)))
      (log/info "Starting break for " pause-duration "milliseconds")
      (Thread/sleep pause-duration)
      (log/info "Ending break")))
    (save-valihuudot! valihuudot info))

(defn tweet [valihuudot info]
  (if (> (count valihuudot) 0)
    (log/info (str "Twiittaan välihuudot pöytäkirjasta: "
                                (:memo-url info)))

    (log/info (str "Valitettavasti pöytäkirjassa: "
                                (:memo-url info) " - ei ollut yhtään
                                välihuutoa.")))
 (let [amount (count valihuudot)
       pause-duration (int (/ 36000000 (max amount 1)))
       huudot (distinct (map #(trim-text %) valihuudot))]
  (doseq [msg huudot]
    (log/info "Now tweeting: " msg)
    ;(log/info "Amount of valihuudot " amount)
    ;(log/info "Starting break for " pause-duration "milliseconds")
    ;(log/info "info: " info)

    )))