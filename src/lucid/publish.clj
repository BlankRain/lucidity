(ns lucid.publish
  (:require [hara.io
             [file :as fs]
             [project :as project]
             [watch :as watch]]
            [hara.namespace.import :as ns]
            [lucid.publish
             [prepare :as prepare]
             [render :as render]
             [theme :as theme]]
            [clojure.java.io :as io]))

(ns/import lucid.publish.theme [deploy])

(def ^:dynamic *output* "docs")

(def ^:dynamic *watchers* {})

(defn output-path
  "creates a path representing where the output files will go"
  {:added "1.2"}
  [project]
  (let [output-dir (or (-> project :publish :output)
                         *output*)]
    (fs/path (:root project) output-dir)))

(defn copy-assets
  "copies all theme assets into the output directory
 
   ;; copies theme using the `:copy` key into an output directory
   (copy-assets)"
  {:added "1.2"}
  ([]
   (let [project (project/project)
         theme (-> project :publish :theme)
         settings (theme/load-settings theme)]
     (copy-assets settings project)))
  ([settings project]
   (let [template-dir (theme/template-path settings project)
         output-dir (output-path project)]
     (prn template-dir output-dir settings)
     (doseq [entry (:copy settings)]
       (prn entry)
       (let [dir   (fs/path template-dir entry)
             files (->> (fs/select dir)
                        (filter fs/file?))]
         (doseq [in files]
           (let [out (fs/path output-dir (str (fs/relativize dir in)))]
             (fs/create-directory (fs/parent out))
             (fs/copy-single in out {:options [:replace-existing :copy-attributes]}))))))))

(defn load-settings
  "copies all theme assets into the output directory
 
   ;; {:email \"z@caudate.me\", :date \"06 October 2016\" ...}
   (load-settings)"
  {:added "1.2"}
  ([] (load-settings {} (project/project)))
  ([opts project]
   (let [theme (or (:theme opts)
                   (-> project :publish :theme))
         settings (merge (theme/load-settings theme project)
                         opts)]
     (when (:refresh settings)
       (theme/deploy settings project)
       (copy-assets settings project))
     settings)))

(defn add-lookup
  "adds a namespace to file lookup table if not existing"
  {:added "1.2"}
  [project]
  (if (:lookup project)
     project
    (assoc project :lookup (project/file-lookup project))))

(defn publish
  "publishes a document as an html
 
   ;; publishes the `index` entry in `project.clj`
   (publish \"index\")
 
 
   ;; publishes `index` in a specific project with additional options
   (publish \"index\"
            {:refresh true :theme \"bolton\"}
            (project/project <PATH>))"
  {:added "1.2"}
  ([] (publish [*ns*] {} (project/project)))
  ([x] (cond (map? x)
             (publish [*ns*] x (project/project))

             :else
             (publish x {} (project/project))))
  ([inputs opts project]
   (let [project (add-lookup project)
         settings (load-settings opts project)
         inputs (if (vector? inputs) inputs [inputs])
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
  "publishes all documents as html
 
   ;; publishes all the entries in `:publish :files`
   (publish-all)
 
 
   ;; publishes all entries in a specific project
   (publish-all {:refresh true :theme \"bolton\"}
                (project/project <PATH>))"
  {:added "1.2"}
  ([] (publish-all {} (project/project)))
  ([opts project]
   (let [project  (add-lookup project)
         settings (load-settings opts project)
         template (theme/template-path settings project)
         output   (output-path project)
         files (-> project :publish :files keys vec)]
     (publish files settings project))))

(defn unwatch
  "removes the automatic publishing of documentation files
 
   (unwatch)"
  {:added "1.2"}
  ([] (unwatch (project/project)))
  ([{:keys [root] :as project}]
   (when-let [watchers (get *watchers* root)]
     (alter-var-root #'*watchers* dissoc root)
     (mapv watch/stop-watcher watchers))))

(defn watch
  "automatic  publishing of documentation files
 
   (watch)"
  {:added "1.2"}
  ([]
   (watch {} (project/project)))
  ([opts {:keys [root] :as project}]
   (unwatch project)
   (let [files (->> project
                    :publish
                    :files
                    (map (fn [[k v]]
                           [(-> v :input io/file) k]))
                    (group-by #(.getParentFile (first %))))
         inputs   (map (fn [[dir vs]]
                         (let [names (mapv #(.getName (first %)) vs)]
                           [(str root "/" dir)
                            names
                            (zipmap names (map second vs))]))
                       files)
         watchers (mapv (fn [[dir files lookup]]
                          (-> (watch/watcher [dir]
                                             (fn [_ file]
                                               (when-let [index (lookup (.getName file))]
                                                 (println "publish:" index)
                                                 (publish [(lookup (.getName file))] {} project)))
                                             {:recursive false
                                              :filter files})
                              (watch/start-watcher)))
                        inputs)]
     (alter-var-root #'*watchers* assoc root watchers)
     watchers)))
