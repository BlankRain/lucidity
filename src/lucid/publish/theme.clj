(ns lucid.publish.theme
  (:require [hara.io
             [project :as project]
             [file :as fs]]
            [clojure.java.io :as io]))

(defonce ^:dynamic *default* "stark")

(defonce ^:dynamic *path* "template")

(defn load-var
  "" [ns var]
  (-> (symbol (str ns "/" var))
      resolve
      deref))

(defn load-settings
  ""
  ([] (load-settings nil))
  ([theme] (load-settings theme (project/project)))
  ([theme project]
   (let [current (java.util.Date.)
         theme (or theme (-> project :publish :theme) *default*)
         opts  (-> project
                   :publish
                   :template
                   (assoc :theme theme
                          :date (-> (java.text.SimpleDateFormat. "dd MMMM yyyy")
                                    (.format current))
                          :time (-> (java.text.SimpleDateFormat. "HH mm")
                                    (.format current))))
         ns (cond (string? theme)
                  (symbol (str "lucid.publish.theme." theme))

                  (symbol? theme) theme

                  :else (throw (Exception. (format "Cannot load theme: %s" theme))))
         settings (do (require ns)
                      (load-var ns "settings"))]
     (->> (:render settings)
          (reduce-kv (fn [out k v]
                       (assoc-in out [:defaults k] [:fn (load-var ns v)]))
                     settings)
          (merge opts)))))

(defn apply-settings
  "" [f & args]
  (let [project (project/project)
        theme   (-> project :publish :theme)
        settings (load-settings theme project)]
    (apply f (concat args [settings project]))))

(defn template-path
  ""
  ([] (apply-settings template-path))
  ([settings project]
   (let [template-dir (or (-> project :publish :template :path)
                          *path*)]
     (fs/path (:root project) template-dir (:theme settings)))))
    
(defn refresh?
  ""
  ([] 
   (apply-settings refresh?))
  ([settings project]
   (boolean (-> project :publish :template :refresh))))

(defn deployed?
  ""
  ([]
   (apply-settings deployed?))
  ([settings project]
   (let [target  (template-path settings project)]
     (fs/exists? target))))

(defn deploy
  ""
  ([]
   (apply-settings deploy))
  ([settings project]
   (let [target   (template-path settings project)
         inputs   (mapv (juxt (fn [path]
                                    (-> (str (:resource settings) "/" path)
                                        (io/resource)))
                                  identity)
                            (:manifest settings))]
     (doseq [[resource filename] inputs]
       (try
         (let [out (fs/path target filename)]
           (fs/create-directory (fs/parent out))
           (fs/write (.openStream resource)
                     out
                     {:options [:replace-existing]})
           (.closeStream resource))
         (catch Exception e
           (print "Cannot Deploy Filename:" filename)
           ;;(throw e)
           ))))))

(comment
  (template-path)
  (deploy)
  (:version (load-settings))
  
  {:theme "martell",
   :path "template",
   :resource "html/martell",
   :copy ["assets"],
   :render {:article "render-article", :navigation "render-navigation"},
   :defaults {:template "article.html",
              :navbar [:file "partials/navbar.html"]
              :sidebar [:file "partials/sidebar.html"]
              :footer [:file "partials/footer.html"]
              :dependencies [:file "partials/deps-web.html"]}
   :fn #{:article :navigation}}

  (lucid.unit/scaffold)
  )
