(ns lucid.insight
  (:require [lucid.insight.bundle :as bundle]
            [lucid.insight.viewer :as viewer]))

(defn insight
  "creates a viewer for the bundle"
  {:added "0.1"}
  ([] (insight [#"src"] {}))
  ([regexs] (insight regexs {}))
  ([regexs options]
   (let [bundle (bundle/bundle regexs)
         viewer (viewer/viewer {:bundle bundle}
                               options)]
     viewer)))
