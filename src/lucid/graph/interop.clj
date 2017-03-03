(ns lucid.graph.interop
  (:require [hara.object :as object]
            [hara.common.string :as string]
            [lucid.graph.util :as util]
            [lucid.graph.draw.dom :as dom]
            [lucid.graph.draw.css :as css]))

(defn get-attributes
  "gets the attribute of a graph, node or element as a map"
  {:added "0.1"}
  [element]
  (let [res (reduce (fn [out k]
                      (assoc out (keyword k) (.getAttribute element k)))
                    {}
                    (.getAttributeKeySet element))]
    (if-not (empty? res)
      (dissoc res :ui.stylesheet :ui.title))))

(defn set-attributes
  "sets the attribute of a graph, node or element in the form of a map"
  {:added "0.1"}
  [element attrs]
  (reduce-kv (fn [element k v]
               (if (or (nil? v)
                       (and (vector? v)
                            (empty? v)))
                 (.removeAttribute element (string/to-string k))
                 (.setAttribute element (string/to-string k) (util/attribute-array v)))
               element)
             element
             (dissoc attrs :ui.stylesheet :ui.title)))


(object/map-like

 org.graphstream.graph.implementations.AbstractNode
 {:tag "node"
  :read  {:methods {:id #(-> % .getId keyword)
                    :attributes get-attributes}}
  :write {:empty   (fn [_] (throw (Exception. "Not Implemented")))
          :methods {:attributes set-attributes}}})

(object/map-like
 org.graphstream.graph.implementations.AbstractEdge
 {:tag "edge"
  :read {:methods {:id #(vector (-> % .getSourceNode str keyword)
                                (-> % .getTargetNode str keyword))
                   :attributes get-attributes}}
  :write {:empty   (fn [_] (throw (Exception. "Not Implemented")))
          :methods {:attributes set-attributes}}}

 org.graphstream.graph.implementations.AbstractGraph
 {:tag "graph"
  :include [:node-set :edge-set :strict? :index :step]
  :read {:methods {:attributes get-attributes
                   :dom dom/get-dom
                   :style css/get-stylesheet
                   :title #(.getAttribute % "ui.title")}}
  :write {:empty   (fn [_] (throw (Exception. "Not Implemented")))
          :methods {:attributes set-attributes
                    :dom dom/set-dom
                    :style css/set-stylesheet
                    :title #(.setAttribute %1 "ui.title" (util/attribute-array %2))}}})

(macroexpand-1 '(object/map-like
                org.graphstream.graph.implementations.AbstractGraph
                {:tag "graph"
                 :include [:node-set :edge-set :strict? :index :step]
                 :read {:methods {:attributes get-attributes
                                  :dom dom/get-dom
                                  :style css/get-stylesheet
                                  :title #(.getAttribute % "ui.title")}}
                 :write {:empty   (fn [_] (throw (Exception. "Not Implemented")))
                         :methods {:attributes set-attributes
                                   :dom dom/set-dom
                                   :style css/set-stylesheet
                                   :title #(.setAttribute %1 "ui.title" (util/attribute-array %2))}}}))

(object/map-like

 org.graphstream.ui.view.View
 {:tag "ui.view"
  :include [:x :y :camera]}
 
 org.graphstream.ui.swingViewer.util.GraphMetrics
 {:tag "metrics"}

 org.graphstream.ui.geom.Point2
 {:tag "point"
  :read {:methods {:x #(.x %)
                   :y #(.y %)}}}

 org.graphstream.ui.geom.Point3
 {:tag "point"
  :read {:methods {:x #(.x %)
                   :y #(.y %)
                   :z #(.z %)}}}

 org.graphstream.ui.geom.Vector2
 {:tag "vector"
  :read {:methods {:x #(.x %)
                   :y #(.y %)}}}

 org.graphstream.ui.geom.Vector3
 {:tag "vector"
  :read {:methods {:x #(.x %)
                   :y #(.y %)
                   :z #(.z %)}}})
