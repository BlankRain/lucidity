(ns lucid.git.interop.object-id-test
  (:require [lucid.git.interop :as interop]
            [hara.object :as object]
            [hara.test :refer :all])
  (:import [org.eclipse.jgit.lib AnyObjectId ObjectId]))

(def id "794766b075c502112806f6731dc172dda37e0333")

(fact "returns the same id when it is transformed"
 (object/to-data (object/from-data id ObjectId))
 => id)
