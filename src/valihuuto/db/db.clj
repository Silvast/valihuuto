(ns valihuuto.db.db
  (:require [next.jdbc :as jdbc]
            [valihuuto.config :refer [config]]
            [clojure.tools.logging :as log]
            [clj-time.local :as l]
            [java-time :as jt]
            [clj-time.coerce :as c]))

(def ds (jdbc/get-datasource (:database-url config)))

(defn get-last-tweeted
  "Find latest tweeted versio"
  []
  (let [result (jdbc/execute! ds
                              ["SELECT id, created_at,
                              viimeisin_twiitattu_pvm,
                              versio,
                              to_char(viimeisin_twiitattu_pvm, 'YYYY') as year
                              FROM tila
                              ORDER BY created_at DESC LIMIT 1;"])]
    (if-not (empty? result)
      {:id (:tila/id (first result)),
       :viimeisin-twiitattu-pvm
       (:tila/viimeisin_twiitattu_pvm (first result)),
       :versio (:tila/versio (first result))
       :year (:year (first result))})))


(defn get-istuntotauko-tweeted
  "Find newly tweeted huutos from particular istuntokausi"
  [kausi]
  (let [kausi_year (:year kausi)
        kausi_part (:part kausi)
        result
        (jdbc/execute! ds
                       ["SELECT * FROM istuntotauko_tila WHERE istuntokausi_year
                   = ? AND istuntokausi_part = ?
                   ORDER BY uudelleen_twiitattu DESC LIMIT 1" kausi_year
                        kausi_part])]
    (if-not (empty? result)
      {:id (:istuntotauko_tila/id (first result)),
       :uudelleen-twiitattu-pvm
       (:istuntotauko_tila/uudelleen_twiitattu (first result)),
       :versio (:istuntotauko_tila/poytakirja_versio (first result))
       :year (:istuntotauko_tila/istuntokausi_year (first result))
       :part (:istuntotauko_tila/istuntokausi_part (first result))})))

(defn get-first-versio [kausi]
  (let [kausi_start (jt/local-date (jt/start (:kausi kausi)) "UTC+3")
        kausi_end (jt/local-date (jt/end (:kausi kausi)) "UTC+3")
        result (jdbc/execute! ds
                              ["SELECT poytakirja_versio FROM valihuudot
                       WHERE huudettu >= ? AND huudettu <  ?" kausi_start
                               kausi_end])]

    (Integer. (:valihuudot/poytakirja_versio (first result)))))

(defn get-huudot [kausi versio]
  (let [kausi_year (:year kausi)
        versio (str versio)
        result (jdbc/execute! ds
                              ["SELECT valihuuto FROM valihuudot WHERE
                       extract(year from huudettu)
                   = ? AND poytakirja_versio = ?" kausi_year
                               versio])]
    (if-not (empty? result)
      (map #(:valihuudot/valihuuto %) result))))


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

(defn save-instuntotauko-tila [kausi versio]
  (let [kausi_year (:year kausi)
        kausi_part (:part kausi)
        version (str versio)
        huudettu
        (:valihuudot/huudettu (first
                               (jdbc/execute! ds
                                              ["SELECT huudettu FROM valihuudot WHERE
                       extract(year from huudettu)
                   = ? AND poytakirja_versio = ? LIMIT 1" kausi_year
                                               version])))
        twiitattu (jt/offset-date-time)]


    (jdbc/execute! ds
                   ["INSERT INTO istuntotauko_tila(istuntokausi_year,
                   istuntokausi_part, poytakirja_versio, huudettu,
                   uudelleen_twiitattu)
                 VALUES (?,?,?,?,?)" kausi_year kausi_part version huudettu
                    twiitattu])))
