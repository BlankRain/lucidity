(ns lucid.insight.format-test
  (:use hara.test)
  (:require [lucid.insight.format :refer :all]))

^{:refer lucid.insight.format/format-label :added "0.1"}
(fact "formats label of according to specification"
  (format-label 'x.y/z {:label :full})
  => "x.y/z"

  (format-label 'x.y/hello {:label :name})
  => "hello"

  (format-label 'x.y.z/hello {:label :partial})
  => "y.z/hello"

  (format-label 'x.y.z/hello {:label :partial :skip 2})
  => "z/hello")
