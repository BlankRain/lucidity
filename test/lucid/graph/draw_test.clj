(ns lucid.graph.draw-test
  (:use hara.test)
  (:require [lucid.graph.draw :refer :all]
            [hara.object :as object])
  (:import  [org.graphstream.graph.implementations MultiGraph]))



^{:refer lucid.graph.draw/graph :added "0.1"}
(fact "creates a di-graph for visualization"
  (-> (graph {:title "Hello World"
              :dom {:nodes {:a {:label "A"
                                :ui.class "top" }
                            :b {:label "B"}
                            :c {:label "C"}}
                    :edges {[:a :b] {:label "a->b"}}}})
      (object/to-data))
  => {:title "Hello World",
      :dom {:nodes {:a {:label "A", :ui.class "top"},
                    :b {:label "B"},
                    :c {:label "C"}},
            :edges {[:a :b] {:label "a->b"}}},
      :attributes {}, :step 0.0, :index 0, :strict? true,
      :edge-set [{:attributes {:label "a->b"}, :id [:a :b]}],
      :node-set [{:attributes {:label "C"}, :id :c}
                 {:attributes {:label "B"}, :id :b}
                 {:attributes {:label "A", :ui.class "top"}, :id :a}]})

^{:refer lucid.graph.draw/element :added "0.1"}
(fact "accesses the element within a graph"
  
  (-> (element +current-graph+ :a)
      object/to-data)
  => {:attributes {:label "A", :ui.class "top"}, :id :a})

^{:refer lucid.graph.draw/display :added "0.1"}
(fact "displays the graph in a seperate window")

^{:refer lucid.graph.draw/add-node-listener :added "0.1"}
(fact "adds a listener for updates to node click events")

^{:refer lucid.graph.draw/add-key-listener :added "0.1"}
(fact "adds a listener for key change events")
