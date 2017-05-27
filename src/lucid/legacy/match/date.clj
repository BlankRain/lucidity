(ns lucid.legacy.match.date
  (:use [lucid.legacy.match.java :only [bean-match]]))

;; # Date Extension
;;
;; This is an example of how to enable Java Beans for pattern matching.

(bean-match java.util.Date)
