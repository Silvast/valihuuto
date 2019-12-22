(ns valihuuto.config
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [environ.core :refer [env]]))

(def ^:private default-file "configuration/default.edn")

(defn- load-config [file]
  (with-open [reader (io/reader file)]
    (edn/read (java.io.PushbackReader. reader))))

(defn get-config-file []
  (or (System/getenv "CONFIG")
      (System/getProperty "config")
      (load-config default-file)))

(def config (get-config-file))