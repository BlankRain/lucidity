(ns lucid.aether.base-test
  (:use hara.test)
  (:require [lucid.aether.base :refer :all]))

^{:refer lucid.aether.base/aether :added "1.1"}
(fact "creates an `Aether` object"

  (aether)
  => (contains
      {:repositories [{:id "clojars",
                       :type "default",
                       :url "https://clojars.org/repo"}
                      {:id "central",
                       :type "default",
                       :url "https://repo1.maven.org/maven2/"}],
       :system org.eclipse.aether.RepositorySystem
       :session org.eclipse.aether.RepositorySystemSession}))
