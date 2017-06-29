(ns lucid.core.code.source
  (:require [rewrite-clj.zip :as source]
            [clojure.java.io :as io]
            [lucid.query :as query]))

(defn analyse-source-file
  "analyses a source file for namespace and function definitions
   (analyse-source-file \"example/src/example/core.clj\" {})
   => '{example.core
        {foo
         {:source \"(defn foo\\n  [x]\\n  (println x \\\"Hello, World!\\\"))\"}}}"
  {:added "1.1"}
  [file]
  (let [zloc (source/of-file file)
        nsp  (->  (query/$ zloc [(ns | _ & _)] {:walk :top})
                  first)
        fns  (->> (query/$ zloc [(#{defn defmulti defmacro} | _ ^:%?- string? ^:%?- map? & _)]
                           {:return :zipper :walk :top})
                  (map (juxt source/sexpr
                             (comp #(hash-map :source {:code (source/string %)
                                                       :line (meta (source/node %))
                                                       :path file})
                                   source/up)))
                  (into {}))]
    {nsp fns}))
