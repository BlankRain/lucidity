(ns lucid.graph.interop
  (:use hara.test))

^{:refer lucid.graph.interop/get-attributes :added "0.1"}
(fact "gets the attribute of a graph, node or element as a map")

^{:refer lucid.graph.interop/set-attributes :added "0.1"}
(fact "sets the attribute of a graph, node or element in the form of a map")
