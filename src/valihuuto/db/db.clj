(ns valihuuto.db.db
  (:require [next.jdbc :as jdbc]
            [valihuuto.config :refer [config]]
            [clojure.tools.logging :as log]
            [clj-time.local :as l]
            [clj-time.coerce :as c]))

(def ds (jdbc/get-datasource (:database-url config)))

(defn get-last-tweeted
  "Find latest tweeted versio"
  []
  (let [result (jdbc/execute! ds ["SELECT * FROM tila
            WHERE versio = (SELECT MAX (versio)
            FROM tila)"])]
    (when (seq result)
      {:id (:tila/id (first result)),
       :viimeisin-twiitattu-pvm
       (:tila/viimeisin_twiitattu_pvm (first result)),
       :versio (:tila/versio (first result))})))

(defn save-tweeted-valihuuto! [msg huudettu memo-versio]
  (log/info "from db: " msg)
  (log/info "from db huudettu: " huudettu)
  (log/info "from db memo-versio" memo-versio)
  (jdbc/execute! ds
                 ["INSERT INTO valihuudot(valihuuto, poytakirja_versio,
                 huudettu,twiitattu)
                 VALUES (?,?,?,?)" msg memo-versio (c/to-sql-date huudettu)
                  (c/to-sql-date (l/local-now))]))

(defn save-tweeted-tila! [pvm versio]
  (jdbc/execute! ds
                 ["INSERT INTO tila(viimeisin_twiitattu_pvm, versio)
                 VALUES (?,?)" (c/to-sql-date pvm) (Integer/parseInt versio)]))
