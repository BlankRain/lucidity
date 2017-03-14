(ns leiningen.lucid
  (:require [leiningen.lucid.setup :as setup]
            [lucid.publish :as publish]
            [lucid.unit :as unit]))

(defn lucid
  "
   metadata and documentation management:
   usage:
     lein lucid (watch)  - default, watches project for changes and updates documentation accordingly
     lein lucid docs     - generates documentation from project
     lein lucid import   - imports docstrings from test files
     lein lucid purge    - purges docstrings from code
  "
  [project & args]
  (with-redefs
    [clojure.tools.reader.edn/read-keyword leiningen.lucid.setup/read-keyword]
    (case (first args)
      nil        (lucid project "watch")
      "watch"    (do (unit/import)
                     (publish/publish-all)
                     (publish/watch)
                     (unit/watch))
      "publish"  (publish/publish-all)
      "import"   (unit/import)
      "purge"    (unit/purge)
      "help"     (println (:doc (meta lucid)))
      (lucid project "help"))))

(comment
  (lucid project "init")
  (lucid project "publish")
)