(ns lucid.distribute.manifest-test
  (:use hara.test)
  (:require [lucid.distribute.manifest :refer :all]
            [lucid.distribute.manifest.graph.external :as external]
            [hara.io.project :as project]))

^{:refer lucid.distribute.manifest/clj-version :added "1.2"}
(fact "returns the clojure version of a project"

  (clj-version (project/project "example/distribute.advance/project.clj"))
  => "1.6.0")

^{:refer lucid.distribute.manifest/create-root-entry :added "1.2"}
(fact "creates the root entry")

^{:refer lucid.distribute.manifest/create-branch-entry :added "1.2"}
(fact "creates the individual branch entry")

^{:refer lucid.distribute.manifest/manifest :added "1.2"}
(fact "creates a manifest for further processing"
  
  (-> (project/project "example/distribute.advance/project.clj")
      (manifest)
      :root)
  => (contains {:name 'blah
                :artifact "blah"
                :group "blah"
                :version "0.1.0-SNAPSHOT"
                :dependencies
                (contains '[[org.clojure/clojure "1.6.0"]
                            [im.chit/vinyasa.maven "0.3.1"]
                            [blah/blah.common "0.1.0-SNAPSHOT"]
                            [blah/blah.core "0.1.0-SNAPSHOT"]
                            [blah/blah.util.array "0.1.0-SNAPSHOT"]
                            [blah/blah.util.data "0.1.0-SNAPSHOT"]
                            [blah/blah.web "0.1.0-SNAPSHOT"]
                            [blah/blah.jvm "0.1.0-SNAPSHOT"]
                            [blah/blah.resources "0.1.0-SNAPSHOT"]]
                          :in-any-order)}))
