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
  (let [response
        (rest/statuses-update :oauth-creds creds :params
                              {:status
                               (str "Twiittaan välihuudot pöytäkirjasta: "
                                    (:memo-url info))})
        status-id (atom (:id (:body response)))]
    (log/info "tweet-api response: " @status-id)
    (log/info "tweet-api body: " (:body response))
    (doseq [msg valihuudot]
      (log/info "Now tweeting: " msg)
      (log/info "info: " info)
      (try
        (let [reply-response (rest/statuses-update :oauth-creds creds :params
                                                   {:status msg
                                                    ;:in-reply-to-status-id
                                                    ;        (deref status-id)
                                                    })]
          ;(swap! status-id (:id (:body reply-response)))
          (log/info "New id: " (:id (:body reply-response)))
          (log/info "New id type: " (type (:id (:body reply-response))))
          )
        (catch Exception e
          (log/warn "Could not send a tweet, countered error: " e)))
      (try
        (do
         (db/save-tweeted-valihuuto! msg (:huudettu info) (:memo-version info))
         (db/save-tweeted-tila! (:huudettu info) (:memo-version info)))
      (catch Exception e
        (log/warn "Could not save the valihuuto: " e)))
      (Thread/sleep 300000))))
