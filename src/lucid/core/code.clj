(ns lucid.core.code
  (:require [lucid.core.code
             [test :as test]
             [source :as source]
             [setup :as setup]]
            [rewrite-clj.node :as node]
            [hara.string.prose :as prose]
            [clojure.string :as string]
            [hara.io.file :as fs]))

(defn analyse-file-fn
  ([path time]
   (cond (.endsWith (str path) "_test.clj")
         (test/analyse-test-file path)

         :else
         (source/analyse-source-file path)))
  ([type path time]
   (case type
     :source (source/analyse-source-file path)
     :test (test/analyse-test-file path))))

(def analyse-file-fn* (memoize analyse-file-fn))

(defn analyse-file
  "analyses a source or test file for information
 
   (analyse-file \"src/lucid/core/code.clj\")
   => (contains-in
       {'lucid.core.code
        {'analyse-file
         {:source {:code string?,
                   :line {:row number?
                          :col number?
                          :end-row number?
                          :end-col number?},
                   :path \"src/lucid/core/code.clj\"}}}})
 
   (analyse-file \"test/lucid/core/code_test.clj\")
   => (contains-in
       {'lucid.core.code
       {'analyse-file
         {:test {:code vector?
                 :line {:row number?
                          :col number?
                          :end-row number?
                          :end-col number?}
                 :path \"test/lucid/core/code_test.clj\"},
          :meta {:added \"1.2\"},
          :intro \"analyses a source or test file for information\"}}})"
  {:added "1.2"}
  ([path]
   (analyse-file-fn* path (-> (fs/path path)
                              fs/attributes
                              :last-modified-time)))
  ([type path]
   (analyse-file-fn* type path (-> (fs/path path)
                                   fs/attributes
                                   :last-modified-time))))
        
(defn join-nodes
  "joins nodes together from a test
 
   (-> (analyse-file \"test/lucid/core/code_test.clj\")
       (get-in ['lucid.core.code 'analyse-file :test :code])
       (join-nodes))
   => string?"
  {:added "1.2"}
  [docs]
  (->> docs
       (map (fn [node]
              (let [res (node/string node)]
                (cond (and (not (node/whitespace? node))
                           (not (node/comment? node))
                           (string? (node/value node)))
                      (prose/escape-newlines res)

                      :else res))))
       (string/join)))

(defn source-namespace
  "look up the source file, corresponding to the namespace
 
   (source-namespace 'lucid.unit)
   => 'lucid.unit
   
   (source-namespace 'lucid.unit-test)
   => 'lucid.unit"
  {:added "1.2"}
  [ns]
  (let [sns (if (instance? clojure.lang.Namespace ns)
              (str (.getName ns))
              (str ns))
        sns (if (.endsWith (str sns) "-test")
              (subs sns 0 (- (count sns) 5))    
              sns)]
    (symbol sns)))

(defn all-source-vars
  "Finds all the `defn`, `defmulti` and `defmacro` forms in code
 
   (all-source-vars \"src/lucid/core/code.clj\")
   => '[analyse-file join-nodes
        source-namespace
        all-source-vars all-test-vars]"
  {:added "1.2"}
  [file]
  (->> (fs/code file)
       (filter (fn [form] ('#{defn defmacro defmulti} (first form))))
       (map second)))

(defn all-test-vars
  "Finds all `comment`, `fact` and `facts` forms in code
 
   (all-test-vars \"test/lucid/core/code_test.clj\")
   => '[analyse-file join-nodes
        source-namespace
        all-source-vars all-test-vars]"
  {:added "1.2"}
  [file]
  (->> (fs/code file)
       (filter (fn [form] ('#{fact facts comment} (first form))))
       (keep (comp :refer meta))
       (map (comp symbol name))))

