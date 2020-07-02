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

(defn tweet [valihuudot info]
  (if (> (count valihuudot) 0)
    (rest/statuses-update :oauth-creds creds :params
                          {:status
                           (str "Twiittaan välihuudot pöytäkirjasta: "
                                (:memo-url info))})

    (rest/statuses-update :oauth-creds creds :params
                          {:status
             (format "Valitettavasti pöytäkirjassa: %s ei ollut välihuutoja."
                     (:memo-url info) )}))

  (db/save-tweeted-tila! (:huudettu info) (:memo-version info))
  (let [amount (count valihuudot)
        pause-duration (int (/ 36000000 (max amount 1)))]
    (log/info "Found " amount " of valihuudot")
    (doseq [msg valihuudot]
      (log/info "Now tweeting: " msg)
      (try
        (rest/statuses-update :oauth-creds creds :params
                              {:status msg})
        (catch Exception e
          (log/warn "Could not send a tweet, countered error: " e)))
      (log/info "Starting break for " pause-duration "milliseconds")
      (Thread/sleep  pause-duration)
      (log/info "Ending break")))
  (save-valihuudot! valihuudot info))

(defn tweet-test [valihuudot info]
  (if (> (count valihuudot) 0)
    (log/info (str "Twiittaan välihuudot pöytäkirjasta: "
                                    (:memo-url info)))
    (log/info (format "Valitettavasti pöytäkirjassa: %s ei ollut välihuutoja"
                                       (:memo-url info))))
  (let [amount (count valihuudot)
        pause-duration (int (/ 36000000 (max amount 1)))]
    (log/info "Found " amount " of valihuudot")
    (doseq [msg valihuudot]
      (try
        (log/info msg)
        (catch Exception e
          (log/warn "Could not send a tweet, countered error: " e)))
      (Thread/sleep  1))))

(defn get-memo-url [kausi versio]
  (str "https://www.eduskunta.fi/FI/vaski/Poytakirja/Documents/PTK_"
       versio "+" (:year kausi) ".pdf" ))

(defn tweet-and-save-istuntotauko [valihuudot kausi versio]
  (let [amount (count valihuudot)
        pause-duration (int (/ 36000000 (max amount 1)))
        memo-url (get-memo-url kausi versio)]
    (log/info (str "Twiittaan uusintana välihuudot pöytäkirjasta: "
                   memo-url))
    (rest/statuses-update :oauth-creds creds :params
                          {:status (str "Twiittaan uusintana välihuudot pöytäkirjasta: "
                                        memo-url)})
    (doseq [msg valihuudot]
      (log/info "Now tweeting: " msg)
      (try
        (rest/statuses-update :oauth-creds creds :params
                              {:status msg})
        (catch Exception e
          (log/warn "Could not send a tweet, countered error: " e)))
      (Thread/sleep pause-duration))
    (db/save-instuntotauko-tila kausi versio)))

(defn tweet-old-from-versio [kausi versio]
  (let [valihuudot (atom ())
        version (atom versio)]
    (while (empty? @valihuudot)
      (do
        (let [uusiversio (swap! version inc)
              huudot (db/get-huudot kausi @version)]
         (if (not-empty huudot)
           (reset! valihuudot huudot))
          (log/info "ei huutoja")
          (log/info "new version" @version)
          (log/info "new valihuuto" @valihuudot))))
    (tweet-and-save-istuntotauko @valihuudot kausi @version)))
