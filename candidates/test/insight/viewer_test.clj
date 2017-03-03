(ns lucid.insight.viewer-test
  (:use hara.test)
  (:require [lucid.insight.viewer :refer :all]
            [lucid.insight.bundle :as bundle]
            [lucid.insight.pipeline :as pipe]))

^{:refer lucid.insight.viewer/add-class :added "0.1"}
(fact "adds a :ui.class to a dom"
  (add-class {:nodes {:a {}}}
             :nodes :a "hello")
  => {:nodes {:a {:ui.class ["hello"]}}})

^{:refer lucid.insight.viewer/remove-class :added "0.1"}
(fact "adds a :ui.class to a dom"
  (remove-class {:nodes {:a {:ui.class ["hello" "world"]}}}
                :nodes :a "hello")
  => {:nodes {:a {:ui.class ["world"]}}})

^{:refer lucid.insight.viewer/viewer :added "0.1"}
(fact "creates a viewer for the bundle")



(comment
  (require '[lucid.insight.bundle :as bundle])
  (require '[lucid.insight.pipeline :as pipe])

  (def b (bundle/bundle #"src/lucid.insight"))

  (def vw (viewer {:bundle b :title "hello"}
                  {:bundle {:highlight-vars #{:lucid.insight.bundle/bundle}}}))
  
  (./refresh)
  (pipe/find-namespace-vars (:forward bd) #{"lucid.insight.viewer"})
  (pipe/find-downstream-vars (:forward bd) #{:lucid.insight.pipeline/call-pipe})
  
  (:options vw)
  


  (comment
  (def vw (viewer {:bundle bd} {}))
  (vw :style +default-style+)


  (-> (vw :dom)
      (add-class :nodes :lucid.insight.bundle/->Bundle "focused_node")
      (manipulate (-> (:bundle vw) :forward) pipe/find-adjacent add-class :nodes :lucid.insight.bundle/->Bundle  "adjacent_node")
      
      )
  )

  
  (count (pipe/call-pipe bd {:bundle {:hide-singletons true}}))
  (count (pipe/call-pipe bd {}))
  
  
  (gs/browse
   {:dom (-> (pipe/elements-pipe
              (pipe/call-pipe bd {:bundle {:reverse-calls false
                                           :hide-singletons true}})
              {:format {:label :initials}}))
    :style +default-style+
    })

  (clojure.set/intersection (pipe/find-singletons (:forward bd))
                            (pipe/find-singletons (:reverse bd)))
  
  
  
  (comment
    h   - hide var
    C-h - clear hidden vars
    H   - hide namespace
    C-H - clear hidden namespaces
    s - select/unselect var 
    S - select/unselect namespace
    c - collapse/uncollapse var
    d - toggle dynamic vars
    r - toggle call-reverse
    i - toggle singleton vars)
  
  
  
  (count (-> bd :reverse keys))

  (clojure.set/difference #{1 2} #{1 3})
  
  
  (vw :interaction)
  (vw :options)
  
  (-> (vw :dom)
      :nodes
      :lucid.insight.viewer/viewer)
  
  (:elements )
  
   (pipe/metas-pipe (pipe/call-pipe bd {:bundle {:reverse-calls false
                                                 :hide-namespaces #{"lucid.insight.util"}
                                                 ;;:select-vars #{:lucid.insight.util/keep-vars}
                                                 :hide-singletons true}})


                    {:format {:label :partial}
                     :bundle {:reverse-calls false
                              :hide-namespaces #{"lucid.insight.util"}
                              ;;:select-vars #{:lucid.insight.util/keep-vars}
                              :hide-singletons true}})
   
   
  

  
  (def bw (gs/browse {:dom {:nodes {:a {}
                                    :b {}}
                            :edges {[:a :b] {}}}
                      :title "Hello World"}))
  (get bw :dom)
  (keys bw)
  
  (:graph :dom :viewer)
  
  (bw :dom {:nodes {:a {:label "a"}
                    :b {:label "b"}}
            :edges {[:a :b] {:label "b->c"}}})

  (bw :dom {:nodes {}
            :edges {[:a :b] {:label "a->b"}}}))
