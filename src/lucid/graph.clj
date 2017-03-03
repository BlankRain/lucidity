(ns lucid.graph
  (:require [lucid.graph.draw :as graph]
            [lucid.graph.draw.dom :as dom]
            [lucid.graph.util :as util]
            [hara.data.diff :as diff]
            [hara.object :as object]))

(defonce +current+ nil)

(defonce +use-advance-viewer+
  (do (System/setProperty "org.graphstream.ui.renderer"
                          "org.graphstream.ui.j2dviewer.J2DGraphRenderer")
      (= (System/getProperty "org.graphstream.ui.renderer")
         "org.graphstream.ui.j2dviewer.J2DGraphRenderer")))

(defrecord Browser []
  clojure.lang.IFn
  (invoke [obj]     {:read  (keys (object/meta-read obj))
                     :write (keys (object/meta-write obj))})
  (invoke [obj k]   (object/get obj k))
  (invoke [obj k v] (object/set obj k v)))

(defmethod print-method Browser
  [v w]
  (.write w (str (into {} v))))

(object/map-like

 Browser
 {:tag "browser"
  :proxy {:graph [:attributes :style :title]}
  :read  {:methods {:dom (fn [b] (-> b :dom deref))}}
  :write {:empty (fn [] (throw (Exception. "Not implemented")))
          :methods {:dom (fn [b dom] (reset! (:dom b) dom) b)}}})

(defn browse
  "returns a browser object for viewing and updating a graph. The browser includes 
   a shadow dom so that any changes reflected within the shadow dom will be reflected in
   the front end"
  {:added "0.1"}
  [{:keys [dom style attributes title options listeners] :as m}]
  (let [graph   (graph/graph {:dom dom :style style :attributes attributes :title title})
        more    (dissoc m :dom :style :attributes :title)
        shadow  (atom dom)
        {:keys [keyboard node]} listeners
        viewer  (graph/display graph)
        viewer  (if node
                  (graph/add-node-listener viewer node)
                  viewer)
        viewer  (if keyboard
                  (graph/add-key-listener viewer keyboard)
                  viewer)
        browser (map->Browser (merge {:graph  graph
                                      :dom    shadow
                                      :viewer viewer}
                                     more))]

    (add-watch shadow :pipeline
               (fn [_ _ p n]
                 (dom/diff-dom graph (diff/diff n p))))
    (alter-var-root #'+current+ (constantly browser))
    browser))
