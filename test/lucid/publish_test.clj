(ns lucid.publish-test
  (:use hara.test)
  (:require [lucid.publish :refer :all]))

^{:refer lucid.publish/output-path :added "1.2"}
(fact "creates a path representing where the output files will go")

^{:refer lucid.publish/copy-assets :added "1.2"}
(comment "copies all theme assets into the output directory"

  ;; copies theme using the `:copy` key into an output directory
  (copy-assets))

^{:refer lucid.publish/load-settings :added "1.2"}
(comment "copies all theme assets into the output directory"

  ;; {:email "z@caudate.me", :date "06 October 2016" ...}
  (load-settings))

^{:refer lucid.publish/add-lookup :added "1.2"}
(fact "adds a namespace to file lookup table if not existing")

^{:refer lucid.publish/publish :added "1.2"}
(comment "publishes a document as an html"

  ;; publishes the `index` entry in `project.clj`
  (publish "index")


  ;; publishes `index` in a specific project with additional options
  (publish "index"
           {:refresh true :theme "bolton"}
           (project/project <PATH>)))

^{:refer lucid.publish/publish-all :added "1.2"}
(comment "publishes all documents as html"

  ;; publishes all the entries in `:publish :files`
  (publish-all)


  ;; publishes all entries in a specific project
  (publish-all {:refresh true :theme "bolton"}
               (project/project <PATH>)))


^{:refer lucid.publish/unwatch :added "1.2"}
(comment "removes the automatic publishing of documentation files"

  (unwatch))


^{:refer lucid.publish/watch :added "1.2"}
(comment "automatic  publishing of documentation files"

  (watch))


(comment
  (publish "lucid-unit")

  (watch)
  )
