(ns lucid.distribute-test
  (:use hara.test)
  (:require [lucid.distribute :refer :all]
            [hara.io.project :as project]))

^{:refer lucid.distribute/install :added "1.2"}
(comment "installs all subpackages according to `:distribute` key"

  (install (project/project)))

^{:refer lucid.distribute/deploy :added "1.2"}
(comment "installs all subpackages according to `:distribute` key"

  (deploy (project/project)))

(comment

  (deploy (project/project "../hara/project.clj"))
  (deploy (project/project)))
