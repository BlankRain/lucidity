(ns lucid.git.interop.dir-cache-test
  (:use hara.test)
  (:require [lucid.git.interop.helpers :refer :all]
            [lucid.git.interop :as interop]
            [hara.object :as object]
            [clojure.java.io :as io]))

(fact "testing dir-cache and dir-cache-entry"

  (def path (str "/tmp/gita/" (java.util.UUID/randomUUID)))
  (def tempdir (io/file path))

  (git-status-call tempdir)
  (spit (str path "/hello.txt") "hello")
  (-> (git-add-call tempdir)
      (object/to-data))
  => {"hello.txt" #{:smudged :merged}}

  (-> (git-add-call tempdir)
      (.getEntry 0)
      (object/to-data))
  => (contains {:merged? true,
                :file-mode "100644",
                :stage 0,
                :object-id string?
                :last-modified number?
                :length number?,
                :path-string "hello.txt"
                :creation-time 0}))
