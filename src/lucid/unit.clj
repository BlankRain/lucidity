(ns lucid.unit
  (:require [lucid.core
             [code :as code]]
            [lucid.unit
             [zipper :as zipper]
             [scaffold :as scaffold]]
            [lucid.query :as query]
            [hara.io
             [file :as fs]
             [project :as project]
             [watch :as watch]]
            [clojure.string :as string]
            [clojure.set :as set]
            [rewrite-clj.zip :as source])
  (:refer-clojure :exclude [import]))

(def ^:dynamic *watchers* {})

(defn import
  "imports unit tests as docstrings
          
   ;; import docstrings for the current namespace
   (import)
 
   ;; import docstrings for a given namespace
   (import 'lucid.unit)
 
   ;; import docstrings for the entire project
   (import :all)"
  {:added "1.2"}
  ([] (import *ns*))
  ([ns] (import ns (project/project)))
  ([ns project] (import ns (project/file-lookup project) project))
  ([ns lookup project]
   (cond (= ns :all)
         (doseq [ns (keys (project/all-files (:source-paths project) {} project))]
           (import ns lookup project))
         
         :else
         (let [ns (code/source-namespace ns)
               src-file  (lookup ns)
               test-file (lookup (symbol (str ns "-test")))
               import-fn (fn [nsp refers]
                           (fn [zloc]
                             (zipper/insert zloc nsp refers)))
               refers (if test-file
                        (code/analyse-file :test test-file)
                        {})]
           (if src-file
             (zipper/walk-file src-file nil refers import-fn))))))

(defn purge
  "purge docstrings and meta from file
          
   ;; removes docstrings for the current namespace  
   (purge)
 
   ;; removes docstrings for a given namespace
   (purge 'lucid.unit)
 
   ;; removes docstrings for the entire project
   (purge :all)"
  {:added "1.2"}
  ([] (purge *ns*))
  ([ns] (purge ns (project/project)))
  ([ns project] (purge ns (project/file-lookup project) project))
  ([ns lookup project]
   (cond (= ns :all)
         (doseq [ns (keys (project/all-files (:source-paths project) {} project))]
           (purge ns lookup project))

         :else
         (let [ns (code/source-namespace ns)
               src-file (lookup ns)
               purge-fn (fn [nsp references] identity)]
           (if src-file
             (zipper/walk-file src-file nil nil purge-fn))))))

(defn missing
  "checks functions that are missing in a given namespace
 
   ;; lists missing tests for current namespace
   (missing)
   
   ;; lists missing tests for specific namespace
   (missing 'lucid.unit)"
  {:added "1.2"}
  ([] (missing *ns*))
  ([ns] (missing ns (project/project)))
  ([ns project] (missing ns (project/file-lookup project) project))
  ([ns lookup project]
   (let [ns (code/source-namespace ns)
         src-file (lookup ns)
         test-file (lookup (symbol (str ns "-test")))
         src-vars (if src-file (code/all-source-vars src-file))
         test-vars (if test-file (code/all-test-vars test-file))]
     {ns (set/difference (set src-vars) (set test-vars))})))

(defn orphaned
  "finds all unit tests that do not have functions
   
   ;; lists orphaned tests for current namespace
   (orphaned)
   
   ;; lists orphaned tests for specific namespace
   (orphaned 'lucid.unit)"
  {:added "1.2"}
  ([] (orphaned *ns*))
  ([ns] (orphaned ns (project/project)))
  ([ns project] (orphaned ns (project/file-lookup project) project))
  ([ns lookup project]
   (let [ns (code/source-namespace ns)
         src-file (lookup ns)
         test-file (lookup (symbol (str ns "-test")))
         src-vars (if src-file (code/all-source-vars src-file))
         test-vars (if test-file (code/all-test-vars test-file))]
     {ns (set/difference (set test-vars) (set src-vars))})))

(defn scaffold
  "builds the unit test scaffolding for the source
 
   ;; generates test scaffolding for current namespace
   (scaffold)
   
   ;; generates test scaffolding for specific namespace
   (scaffold 'lucid.unit)"
  {:added "1.2"}
  ([] (scaffold *ns*))
  ([ns] (scaffold ns (project/project)))
  ([ns project] (scaffold/scaffold ns (project/file-lookup project) project)))

(defn in-order?
  "checks vars in the test file is in correct order
   
   ;; checks ordering for current namespace
   (in-order?)
   
   ;; checks ordering for specific namespace
   (in-order? 'lucid.unit)"
  {:added "1.2"}
  ([] (in-order? *ns*))
  ([ns] (in-order? ns (project/project)))
  ([ns project] (scaffold/in-order? ns (project/file-lookup project) project)))

(defn arrange
  "arranges tests so that vars are in correct order
   
   ;; arranges tests for current namespace
   (arrange)
   
   ;; arranges tests for specific namespace
   (arrange 'lucid.unit)"
  {:added "1.2"}
  ([] (arrange *ns*))
  ([ns] (arrange ns (project/project)))
  ([ns project] (scaffold/arrange ns (project/file-lookup project) project)))

(defn unwatch
  "removes the automatic watching and importing of tests
 
   (unwatch)"
  {:added "1.2"}
  ([] (unwatch (project/project)))
  ([{:keys [root] :as project}]
   (when-let [watcher (get *watchers* root)]
     (alter-var-root #'*watchers* dissoc root)
     (watch/stop-watcher watcher))))

(defn watch
  "automatic imports tests when files change
 
   (watch)"
  {:added "1.2"}
  ([] (watch {} (project/project)))
  ([opts {:keys [root test-paths] :as project}]
   (unwatch project)
   (let [lookup    (project/file-lookup project)
         ns-lookup (zipmap (vals lookup) (keys lookup))
         watcher   (-> (watch/watcher test-paths
                                      (fn [_ file]
                                        (let [ns (ns-lookup (str file))]
                                          (import ns lookup project)))
                                      {:recursive true
                                       :filter ["*.clj"]})
                       (watch/start-watcher))]
     (alter-var-root #'*watchers* assoc root watcher)
     watcher)))

(comment
  (watch)
  (unwatch)
  (:test-paths (project/project))
  (def lookup (project/file-lookup (project/project)))
  @#'lucid.unit/lookup
  
  )
