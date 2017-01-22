(ns lucid.aether.base
  (:require [lucid.aether
             [session :as session]
             [system :as system]]))

(def +defaults+
  {:repositories [{:id "clojars"
                   :type "default"
                   :url "https://clojars.org/repo"}
                  {:id "central"
                   :type "default"
                   :url "http://repo1.maven.org/maven2/"}]})

(defrecord Aether [])

(defmethod print-method Aether
  [v w]
  (.write w (str "#aether" (into {} v))))

(defn aether
  "creates an `Aether` object
 
   (aether)
   => (contains
       {:repositories [{:id \"clojars\",
                        :type \"default\",
                        :url \"http://clojars.org/repo\"}
                       {:id \"central\",
                       :type \"default\",
                        :url \"http://central.maven.org/maven2/\"}],
        :system org.eclipse.aether.RepositorySystem
        :session org.eclipse.aether.RepositorySystemSession})"
  {:added "1.1"}
  ([] (aether {}))
  ([config]
   (let [system  (system/repository-system)
         session (->> (select-keys config [:local-repo])
                      (session/session system))]
     (-> +defaults+
         (merge {:system system
                 :session session})
         (map->Aether)))))
