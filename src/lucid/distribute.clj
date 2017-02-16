(ns lucid.distribute
  (:require [hara.namespace.import :as ns]
            [lucid.package :as package]
            [lucid.distribute
             [common :as common]
             [manifest :as manifest]
             [split :as split]]
            [hara.io
             [file :as fs]
             [project :as project]]))

(ns/import lucid.distribute.manifest [manifest]
           lucid.distribute.split [clean split])

(defn install
  "installs all subpackages according to `:distribute` key
 
   (install (project/project))"
  {:added "1.2"}
  ([] (install (project/project)))
  ([project]
   (install project (manifest/manifest project)))
  ([project manifest]
   (let [packages (split/split project manifest)]
     (doseq [id packages]
       (println "\nInstalling" id)
       (-> (common/interim-path project)
           (str "/branches/" id "/project.clj")
           (project/project)
           (package/install-project)))
     
     (println "\nInstalling Root")
     (-> (common/interim-path project)
         (str "/root/project.clj")
         (project/project)
         (package/install-project)))))

(defn deploy
  "installs all subpackages according to `:distribute` key
 
   (deploy (project/project))"
  {:added "1.2"}
  ([] (deploy (project/project)))
  ([project]
   (deploy project (manifest/manifest project)))
  ([project manifest]
   (deploy project manifest {}))
  ([project manifest opts]
   (let [packages (split/split project manifest)]
     (doseq [id packages]
       (println "\nDeploying" id)
       (try (-> (common/interim-path project)
                (str "/branches/" id "/project.clj")
                (project/project)
                (package/deploy-project opts))
            (catch Throwable t
              (println t)
              (println "FAILED for " id))))
     
     (println "\nDeploying Root")
     (-> (common/interim-path project)
         (str "/root/project.clj")
         (project/project)
         (package/deploy-project opts)))))

(defn deploy-current
  "installs all in the current project
 
   (deploy-current {:authentication {:username \"hello\"
                                    :password \"world\"}})"
  {:added "1.2"}
  ([]
   (deploy-current {}))
  ([opts]
   (let [project  (project/project)
         manifest (manifest/manifest project)]
     (deploy project manifest opts))))
