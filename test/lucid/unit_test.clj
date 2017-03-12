(ns lucid.unit-test
  (:use hara.test)
  (:require [lucid.unit :refer :all])
  (:refer-clojure :exclude [import]))

^{:refer lucid.unit/import :added "1.2"}
(comment "imports unit tests as docstrings"
         
  ;; import docstrings for the current namespace
  (import)

  ;; import docstrings for a given namespace
  (import 'lucid.unit)

  ;; import docstrings for the entire project
  (import :all))

^{:refer lucid.unit/purge :added "1.2"}
(comment "purge docstrings and meta from file"
         
  ;; removes docstrings for the current namespace  
  (purge)

  ;; removes docstrings for a given namespace
  (purge 'lucid.unit)

  ;; removes docstrings for the entire project
  (purge :all))

^{:refer lucid.unit/missing :added "1.2"}
(comment "checks functions that are missing in a given namespace"

  ;; lists missing tests for current namespace
  (missing)
  
  ;; lists missing tests for specific namespace
  (missing 'lucid.unit))

^{:refer lucid.unit/orphaned :added "1.2"}
(comment "finds all unit tests that do not have functions"
  
  ;; lists orphaned tests for current namespace
  (orphaned)
  
  ;; lists orphaned tests for specific namespace
  (orphaned 'lucid.unit))

^{:refer lucid.unit/scaffold :added "1.2"}
(comment "builds the unit test scaffolding for the source"

  ;; generates test scaffolding for current namespace
  (scaffold)
  
  ;; generates test scaffolding for specific namespace
  (scaffold 'lucid.unit))

^{:refer lucid.unit/in-order? :added "1.2"}
(comment "checks vars in the test file is in correct order"
  
  ;; checks ordering for current namespace
  (in-order?)
  
  ;; checks ordering for specific namespace
  (in-order? 'lucid.unit))

^{:refer lucid.unit/arrange :added "1.2"}
(comment "arranges tests so that vars are in correct order"
  
  ;; arranges tests for current namespace
  (arrange)
  
  ;; arranges tests for specific namespace
  (arrange 'lucid.unit))


^{:refer lucid.unit/unwatch :added "1.2"}
(comment "removes the automatic watching and importing of tests"

  (unwatch))


^{:refer lucid.unit/watch :added "1.2"}
(comment "automatic imports tests when files change"

  (watch))


(comment

  (import)

  
  )
