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


^{:refer lucid.package/create-digest :added "1.2"}
(comment "creates a digest given a file and a digest type"

  (create-digest "MD5"
                 "md5"
                 {:file "project.clj"
                  :extension "clj"})
  => {:file "project.clj.md5",
      :extension "clj.md5"})


^{:refer lucid.package/add-digest :added "1.2"}
(comment "adds MD5 and SHA1 digests to all artifacts"

  (add-digest [{:file "project.clj",
                :extension "clj"}])
  => [{:file "project.clj.md5", :extension "clj.md5"}
      {:file "project.clj.sha1", :extension "clj.sha1"}
      {:file "project.clj", :extension "clj"}])

(comment
  (lucid.unit/import)
  )
