(ns lucid.package
  (:require [hara.namespace.import :as ns]
            [hara.io
             [classpath :as classpath]
             [encode :as encode]
             [file :as fs]
             [project :as project]]
            [hara.security :as security]
            [lucid.aether :as aether]
            [lucid.package
             [jar :as jar]
             [pom :as pom]
             [privacy :as privacy]
             [resolve :as resolve]
             [user :as user]]))

(ns/import
 
 lucid.package.pom
 [generate-pom]

 lucid.package.jar
 [generate-jar]

 lucid.package.resolve
 [list-dependencies
  pull
  resolve-with-dependencies])

(defn compile-project
  "creates the jar and pom files
 
   (compile-project (project/project))"
  {:added "1.2"}
  [project]
  (let [jar-file (jar/generate-jar project)
        pom-file (pom/generate-pom project)]
    [jar-file pom-file]))

(defn install-project
  "creates the jar and pom files and installs to local-repo
 
   (install-project (project/project))"
  {:added "1.2"}
  [project]
  (let [[jar-file pom-file] (compile-project project)]
    (-> project
        (select-keys [:group :artifact :version])
        (aether/install-artifact {:artifacts [{:file jar-file
                                               :extension "jar"}
                                              {:file pom-file
                                               :extension "pom"}]}))))

(defn sign-file
  "signs a file with gpg
 
   (sign-file {:file \"project.clj\" :extension \"clj\"}
             {:signing (-> lucid.package.user/LEIN-PROFILE
                            :user)})"
  {:added "1.2"}
  ([{:keys [file extension]}
     {:keys [signing suffix ring-file]
      :or {suffix "asc"
           ring-file user/GNUPG-SECRET}}]
   (let [output (str file "." suffix)
         output-ex (str extension "." suffix)]
     (privacy/sign file output ring-file signing)
     {:file output
      :extension output-ex})))

(defn add-authentication
  "decrypts credentials.gpg and inserts the right authentication
 
   (add-authentication {:id \"clojars\"}
                      {})"
  {:added "1.2"}
  [{:keys [id] :as repository} {:keys [ring-file cred-file]
       :or {cred-file user/LEIN-CREDENTIALS-GPG
            ring-file user/GNUPG-SECRET}}]
  (let [auth-map (read-string (privacy/decrypt cred-file ring-file))]
    (->> auth-map
         (filter (fn [[k _]]
                   (cond (string? k)
                         (= id k)
                         
                         (instance? java.util.regex.Pattern k)
                         (re-find k id))))
         first
         second
         (assoc repository :authentication))))

(defn md5-digest
  "creates an md5-digest file
 
   (md5-digest {:file \"project.clj\"
                :extension \".clj\"})
   => {:file \"project.clj.md5\",
       :extension \".clj.md5\"}"
  {:added "1.2"}
  [{:keys [file extension]}]
  (let [content (-> (fs/read-all-bytes file)
                    (security/digest "MD5")
                    (encode/to-hex))
        file (str file ".md5")
        extension (str extension ".md5")
        _ (spit file content)]
    {:file file :extension extension}))

(defn deploy-project
  "creates the jar and pom files and deploys to clojars
 
   (deploy-project (project/project))"
  {:added "1.2"}
  ([project]
   (deploy-project project {}))
  ([project {:keys [id]
             :or {id "clojars"}}]
   (let [[jar-file pom-file] (compile-project project)
         aether   (aether/aether)
         repository (->> (:repositories aether)
                         (filter #(-> % :id (= id)))
                         first)
         repository (add-authentication repository {})
         artifacts  [{:file jar-file
                      :extension "jar"}
                     {:file pom-file
                      :extension "pom"}]
         
         ;;(comment "Fix CRC24, Signature Verification")
         artifacts (if-let [signing (-> user/LEIN-PROFILE
                                        slurp
                                        read-string
                                        (get-in [:user :signing :gpg-key]))]
                     (->> artifacts
                          (map #(sign-file % {:signing signing}))
                          (concat artifacts))
                     artifacts)
         
         artifacts (->> artifacts
                        (map md5-digest)
                        (concat artifacts))]
     (-> project
         (select-keys [:group :artifact :version])
         (->> (classpath/artifact :coord))
         (aether/deploy-artifact {:artifacts artifacts
                                  :repository repository})))))

(comment
  (require '[lucid.unit :as unit])
  (unit/import)
  )
