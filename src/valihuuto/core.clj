(ns valihuuto.core
  (:require [valihuuto.config :refer [config]]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [clj-time.format :as f]
            [valihuuto.db.db :as db]
            [clojure.tools.logging :as log]
            [valihuuto.db.migrations :as m]
            [valihuuto.huudot :as huudot])
  (:gen-class))

(defn -main
  "Will check the situation with the tweets and tweet if suitable."
  [& args]
    (log/info "Running migrations")
    (m/migrate!)
    (log/info "Migrations done.")
    (log/info "Fetching tweets")
  (huudot/search-huudot))


