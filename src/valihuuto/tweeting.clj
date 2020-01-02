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

(defn tweet [valihuudot info]
  (rest/statuses-update :oauth-creds creds :params
                        {:status (str "Twiitattava pöytäkirja: " (:memo-url
                                                                   info))})
  (doseq [msg valihuudot]
    (log/info "Now tweeting: " msg)
    (log/info "info: " info)
    (try
    (rest/statuses-update :oauth-creds creds :params
     {:status msg})
    (println msg)
    (catch clojure.lang.ExceptionInfo e
      (log/warn "Could not send a tweet, countered error: " e)))
    (db/save-tweeted-valihuuto! msg (:huudettu info) (:memo-version info))
    (db/save-tweeted-tila! (:huudettu info) (:memo-version info))
    (Thread/sleep 5000)))