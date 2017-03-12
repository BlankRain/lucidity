(ns lucid.query.match.pattern-test
  (:use hara.test)
  (:require [lucid.query.match.pattern :refer :all]))

^{:refer lucid.query.match.pattern/pattern-matches :added "1.2"}
(fact "pattern"
  ((pattern-matches ()) ())
  => '(())

  ((pattern-matches []) ())
  => ()
  
  ((pattern-matches '(^:% symbol? ^:? (+ 1 _ ^:? _))) '(+ (+ 1 2 3)))
  => '((^{:% true} symbol? ^{:? 0} (+ 1 _ ^{:? 1} _))))


(set! *print-meta* false)

(comment
  (require 'lucid.distribute)
  (lucid.distribute/deploy)
  )

