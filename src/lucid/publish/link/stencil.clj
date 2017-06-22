(ns lucid.publish.link.stencil
  (:require [clojure.string :as string]))

(def full-citation-pattern
  (string/join ["\\[\\["
                "([^/^\\.^\\{^\\}^\\[^\\]]+)"
                "/"
                "([^/^\\.^\\{^\\}^\\[^\\]]+)"
                "\\]\\]"]))

(def short-citation-pattern
  (string/join ["\\[\\["
                "([^/^\\.^\\{^\\}^\\[^\\]]+)"
                "\\]\\]"]))

(def full-pattern
  (string/join ["\\{\\{"
                "([^/^\\.^\\{^\\}]+)"
                "/"
                "([^/^\\.^\\{^\\}]+)"
                "\\}\\}"]))

(def short-pattern
  (string/join ["\\{\\{"
                "([^/^\\.^\\{^\\}]+)"
                "\\}\\}"]))

(defn transform-stencil
  ""
  [string name tags]
  (-> string
      (.replaceAll full-citation-pattern "[{{$1/$2}}]($1.html#$2)")
      (.replaceAll short-citation-pattern "[{{$1}}](#$1)")
      (.replaceAll full-pattern  "{{$1.$2.number}}")
      (.replaceAll short-pattern (str "{{" name ".$1.number}}"))
      (stencil/render-string tags)))

(defn link-stencil
  ""
  [interim name]
  (let [anchors (assoc (:anchors interim)
                       :PROJECT (:project interim)
                       :DOCUMENT (get-in interim [:articles name :meta]))]
    (update-in interim [:articles name :elements]
               (fn [elements]
                 (->> elements
                      (map (fn [element]
                             (cond (= :paragraph (:type element))
                                   (update-in element [:text]
                                              transform-stencil name anchors)
                                              
                                   (:stencil element)
                                   (update-in element [:code]
                                              transform-stencil name anchors)
                                   
                                   :else element))))))))
