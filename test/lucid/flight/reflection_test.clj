(ns lucid.flight.reflection-test
  (:use hara.test)
  (:require [lucid.flight.reflection :refer :all]
            [clojure.repl :as repl]
            [clojure.string :as string])
  (:refer-clojure :exclude '[.% .%> .? .* .& .> .>ns .>var]))

^{:refer lucid.flight.reflection/.% :added "2.1"}
(fact "Lists class information"
  (.% String)
  => (contains {:modifiers #{:instance :public :final :class},
                :name "java.lang.String"}))

^{:refer lucid.flight.reflection/.%> :added "2.1"}
(fact "Lists the class and interface hierarchy for the class"

  (.%> String)
  => [java.lang.String
      [java.lang.Object
       #{java.io.Serializable
         java.lang.Comparable
         java.lang.CharSequence}]])

^{:refer lucid.flight.reflection/.& :added "2.1"}
(fact "Allow transparent field access and manipulation to the underlying object."
  
  (def a "hello")
  (def >a (.& a))
  (keys >a) => (contains [:hash])

  (do (>a :value (char-array "world"))
      a)
  => "world")

^{:refer lucid.flight.reflection/.? :added "2.1"}
(fact "queries the java view of the class declaration"

  (.? String  #"^c" :name)
  => ["charAt" "checkBounds" "codePointAt" "codePointBefore"
      "codePointCount" "compareTo" "compareToIgnoreCase"
      "concat" "contains" "contentEquals" "copyValueOf"])

^{:refer lucid.flight.reflection/.* :added "2.1"}
(fact "lists what methods could be applied to a particular instance"

  (.* "abc" :name #"^to")
  => ["toCharArray" "toLowerCase" "toString" "toUpperCase"]

  (.* String :name #"^to")
  => (contains ["toString"]))

^{:refer lucid.flight.reflection/.> :added "2.1"}
(fact "Threads the first input into the rest of the functions. Same as `->` but
   allows access to private fields using both `:keyword` and `.symbol` lookup:"

  (.> "abcd" :value String.) => "abcd"

  (.> "abcd" .value String.) => "abcd"

  (let [a  "hello"
        _  (.> a (.value (char-array "world")))]
    a)
  => "world")

^{:refer lucid.flight.reflection/.>var :added "2.1"}
(fact "extracts a class method into a namespace."

  (.>var hash-without [clojure.lang.IPersistentMap without])

  (->> (eval '(clojure.repl/doc hash-without))
       with-out-str
       string/split-lines
       (drop 2))
  =>  ["[[clojure.lang.IPersistentMap java.lang.Object]]"
       "  "
       "member: clojure.lang.IPersistentMap/without"
       "type: clojure.lang.IPersistentMap"
       "modifiers: instance, method, public, abstract"]

  (eval '(hash-without {:a 1 :b 2} :a))
  => {:b 2})

^{:refer lucid.flight.reflection/.>ns :added "2.1"}
(fact "extracts all class methods into its own namespace."

  (map #(.sym %)
       (.>ns test.string String :private #"serial"))
  => '[serialPersistentFields serialVersionUID])