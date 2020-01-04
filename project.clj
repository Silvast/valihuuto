(defproject valihuuto "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-tagsoup "0.3.0" :exclusions [org.clojure/clojure]]
                 [feedme "0.0.3"]
                 [pdfboxing "0.1.14"]
                 [clj-http "3.10.0"]
                 [org.clojure/java.jdbc "0.7.9"]
                 [org.flywaydb/flyway-core "5.2.4"]
                 [org.postgresql/postgresql "42.2.5"]
                 [environ "1.1.0"]
                 [clj-time "0.15.2"]
                 [seancorfield/next.jdbc "1.0.12"]
                 ;;[org.slf4j/slf4j-simple "1.7.5"]
                 [org.apache.logging.log4j/log4j-api "2.11.1"]
                 [org.apache.logging.log4j/log4j-core "2.11.1"]
                 [org.apache.logging.log4j/log4j-slf4j-impl "2.11.1"]
                 [org.clojure/tools.logging "0.4.1"]
                 [environ "1.1.0"]
                 [twitter-api "1.8.0" :exclusions [org.clojure/tools.logging]]]
  :plugins [[lein-cljfmt "0.6.0" :exclusions [org.clojure/tools.cli]]
            [lein-kibit "0.1.6"]
            [lein-bikeshed "0.5.2"]
            [jonase/eastwood "0.3.1"]
            [lein-auto "0.1.3"]
            [lein-ancient "0.6.15"]
            [lein-cloverage "1.0.13"]
            [lein-eftest "0.5.7"]
            [lein-environ "1.1.0"]]
  :main ^:skip-aot valihuuto.core
  :target-path "target/%s"
  :resource-paths ["configuration"]
  :aliases {"checkall" ["with-profile" "+dev" "do"
                        ["kibit"]
                        ["bikeshed"]
                        ["eastwood"]
                        ["cljfmt" "check"]]
            "dbmigrate" ["run" "-m" "valihuuto.db.migrations/migrate!"]
            "dbclean" ["run" "-m" "valihuuto.db.migrations/clean!"]}
  :profiles {:uberjar {:aot :all}})
