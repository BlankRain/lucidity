^{:name "hello-world"
  :template "home.html"
  :title "lucidity"
  :subtitle "tools for clarity"}
(ns lucid.publish
  (:require [hara.io.project :as project]
            [lucid.publish
             [prepare :as prepare]
             [render :as render]
             [template :as template]]
            [clojure.java.io :as io]
            [hara.io.file :as fs]))

(def ^:dynamic *output* "docs")
(def ^:dynamic *template* "template")

(defn publish
  ([] (publish [*ns*]))
  ([inputs]
   (let [project (project/project)
         theme  (-> project :publish :template :theme)
         settings (template/load-settings theme project)]
     (publish inputs settings project)))
  ([inputs settings project]
   (let [inputs (if (vector? inputs) inputs [inputs])
         ns->symbol (fn [x] (if (instance? clojure.lang.Namespace x)
                              (.getName x)
                              x))
         inputs (map ns->symbol inputs)
         interim (prepare/prepare inputs project)
         names (-> interim :articles keys)
         out-dir (fs/path (-> project :root)
                          (or (-> project :publish :output) *output*))]
     (fs/create-directory out-dir)
     (doseq [name names]
       (spit (str (fs/path (str out-dir) (str name ".html")))
             (render/render interim name settings project))))))

(defn publish-all
  ([]
   (let [project (project/project)
         theme  (-> project :publish :template :theme)
         settings (template/load-settings theme project)]
     (publish-all settings project)))
  ([settings project]
   (let [files (-> project :publish :files)]
     (publish (vec (keys files)) settings project))))

(defn copy-template
  ([]
   (let [project (project/project)
         theme  (-> project :publish :template :theme)
         settings (template/load-settings theme project)]
     (copy-template settings project)))
  ([settings project]
   (let [target (or (-> project :publish :template :path)
                    *template*)
         inputs   (mapv (juxt (fn [path]
                                (-> (str (:resource settings) "/" path)
                                    (io/resource)
                                    (.openStream)))
                              identity)
                        (:manifest settings))]
     (doseq [[stream filename] inputs]
       (let [out (fs/path (:root project) target filename)]
         (fs/create-directory (fs/parent out))
         (fs/write stream out))))))

(comment
  (def settings (render/load-settings "martell"))

  (copy-template)
  (publish "index")
  (publish "index")

  (publish "lucid-mind")

  
  
  
  (copy-template "martell")
  (settings)
  
  (java.nio.file.Files/copy (.openStream (io/resource "clojure/core/match.clj"))
                            (fs/path "match.clj")
                            (make-array java.nio.file.CopyOption 0))
  (deploy-template "martell")
  
  (tagged-name (parse/parse-file (lookup-path *ns* (project/project)) (project/project)))
  
  (publish :pdf)
  (publish "index" :html)
  (def interim (publish))

  (keys interim)
  (:articles :meta :namespaces :anchors-lu :anchors)
  
  (:namespaces interim)

  (:anchors interim)

  (keys (get-in interim [:articles "hello-world"]))
  
  (:anchors-lu interim)
  
  (def project (project/project))
  
  (-> (project/project)
      :site
      :files)

  (-> (project/project)
      :root))
  
