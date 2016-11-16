(ns lucid.package-test
  (:use hara.test)
  (:require [lucid.package :refer :all]
            [hara.io.project :as project]))

^{:refer lucid.package/compile-project :added "1.2"}
(comment "creates the jar and pom files"

  (compile-project (project/project)))

^{:refer lucid.package/deploy-project :added "1.2"}
(comment "creates the jar and pom files and deploys to clojars"

  (deploy-project (project/project)))

^{:refer lucid.package/install-project :added "1.2"}
(comment "creates the jar and pom files and installs to local-repo"

  (install-project (project/project)))

^{:refer lucid.package/sign-file :added "1.2"}
(comment "signs a file with gpg"

  (sign-file {:file "project.clj" :extension "clj"}
             {:signing (-> lucid.package.user/LEIN-PROFILE
                           :user)}))

^{:refer lucid.package/add-authentication :added "1.2"}
(comment "decrypts credentials.gpg and inserts the right authentication"

  (add-authentication {:id "clojars"}
                      {}))

(comment
  (lucid.unit/import)
  )
