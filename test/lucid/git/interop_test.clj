(ns lucid.git.interop-test
  (:require [lucid.git.interop :refer :all]
            [hara.test :refer :all]
            [lucid.git.api.repository :as repository])
  (:import org.eclipse.jgit.api.Git))

(comment

  (.? (Git. (repository/repository)) :name)
  )
