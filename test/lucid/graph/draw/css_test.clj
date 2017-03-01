(ns lucid.graph.draw.css-test
  (:use hara.test)
  (:require [lucid.graph.draw.css :refer :all]
            [lucid.graph.draw :as graph]))

^{:refer lucid.graph.draw.css/property-pair :added "0.1"}
(fact "splits up a property pair into a vector for input into a map"

  (property-pair "hello : world")
  => [:hello "world"]
  )

^{:refer lucid.graph.draw.css/emit :added "0.1"}
(fact "transforms a clojure datastructure into a css string"
  
  (emit [[:node {:shape "freeplane"}]])
  => (str "node {\n  shape: freeplane;\n}"))

^{:refer lucid.graph.draw.css/parse :added "0.1"}
(fact "transforms a css-string into a clojure datastructure"

  (parse "node {\n  shape: freeplane;\n}")
  => [[:node {:shape "freeplane"}]])

(fact "emit/parse combination yields the same result" 
  (let [forms [[:edge {:arrow-shape "arrow"
               :arrow-size "10, 10"}]
               [:node {:shape "freeplane"}]]]
    (-> forms 
        emit
        parse)
    => forms))

^{:refer lucid.graph.draw.css/get-stylesheet :added "0.1"}
(fact "accessor function for graph stylesheet property"

  (-> (graph/graph {:style [[:node {:color "red"}]]})
      (get-stylesheet))
  => [[:node {:color "red"}]]
  )


^{:refer lucid.graph.draw.css/set-stylesheet :added "0.1"}
(fact "setter function for graph stylesheet property"
  
  (-> (graph/graph {})
      (set-stylesheet [[:node {:color "red"}]])
      (get-stylesheet))
  => [[:node {:color "red"}]])
