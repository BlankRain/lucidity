(defproject im.chit/lucid "1.3.10"
  :description "tools for code clarity"
  :url "https://www.github.com/zcaudate/lucidity"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :aliases {"test" ["run" "-m" "hara.test" ":exit"]}
  :dependencies [[org.clojure/clojure             "1.8.0"]
                 
                 [im.chit/hara.common.checks      "2.5.6"]
                 [im.chit/hara.concurrent.latch   "2.5.6"]
                 [im.chit/hara.data.diff          "2.5.6"]
                 [im.chit/hara.data.nested        "2.5.6"]
                 [im.chit/hara.event              "2.5.6"]
                 [im.chit/hara.io.archive         "2.5.6"]
                 [im.chit/hara.io.classloader     "2.5.6"]
                 [im.chit/hara.io.classpath       "2.5.6"]
                 [im.chit/hara.io.encode          "2.5.6"]
                 [im.chit/hara.io.file            "2.5.6"]
                 [im.chit/hara.io.project         "2.5.6"]
                 [im.chit/hara.io.watch           "2.5.6"]
                 [im.chit/hara.namespace          "2.5.6"]
                 [im.chit/hara.object             "2.5.6"]
                 [im.chit/hara.reflect            "2.5.6"]
                 [im.chit/hara.security           "2.5.6"]
                 [im.chit/hara.string.case        "2.5.6"]
                 [im.chit/hara.string.prose       "2.5.6"]
                 [im.chit/hara.test               "2.5.6"]
                 
                 [org.eclipse.aether/aether-api "1.1.0"]
                 [org.eclipse.aether/aether-spi "1.1.0"]
                 [org.eclipse.aether/aether-util "1.1.0"]
                 [org.eclipse.aether/aether-impl "1.1.0"]
                 [org.eclipse.aether/aether-connector-basic "1.1.0"]
                 [org.eclipse.aether/aether-transport-wagon "1.1.0"]
                 [org.eclipse.aether/aether-transport-http "1.1.0"]
                 [org.eclipse.aether/aether-transport-file "1.1.0"]
                 [org.eclipse.aether/aether-transport-classpath "1.1.0"]
                 [org.apache.maven/maven-aether-provider "3.3.9"]

                 [org.ow2.asm/asm "5.2"]
                 [org.bouncycastle/bcprov-jdk15on "1.56"]
                 [org.bouncycastle/bcpg-jdk15on "1.56"]
                 [version-clj/version-clj "0.1.2"]
                 [rewrite-clj/rewrite-clj "0.6.0"]
                 [markdown-clj/markdown-clj "0.9.99"]
                 [hiccup/hiccup "1.0.5"]
                 [stencil/stencil "0.5.0"]
                 [org.eclipse.jgit "4.6.0.201612231935-r"]
                 [com.github.dblock/oshi-core "3.4.0"]
                 [garden "1.3.2"]
                 [net.sourceforge.cssparser/cssparser "0.9.22"]
                 [org.graphstream/gs-ui "1.3"
                    :exclusions [[bouncycastle/bcprov-jdk14]
                                 [bouncycastle/bcmail-jdk14]]]
                 [net.bytebuddy/byte-buddy "1.7.0"]
                 ;[w01fe/sniper "0.1.0"]
                 ;[seesaw "1.4.5"]
                 ]
                
  :publish {:theme  "stark"
            
            :template {:site   "lucid"
                       :author "Chris Zheng"
                       :email  "z@caudate.me"
                       :icon   "favicon"
                       :tracking-enabled "true"
                       :tracking "UA-31320512-2"}
            
            :files {"index"
                    {:template "home.html"
                     :input "test/documentation/home_lucidity.clj"
                     :title "lucidity"
                     :subtitle "tools for code clarity"}
                    "lucid-aether"
                    {:input "test/documentation/lucid_aether.clj"
                     :title "aether"
                     :subtitle "wrapper for org.eclipse.aether"}
                    "lucid-core"
                    {:input "test/documentation/lucid_core.clj"
                     :title "core"
                     :subtitle "functions for the code environment"}
                    "lucid-distribute"
                    {:input "test/documentation/lucid_distribute.clj"
                     :title "distribute"
                     :subtitle "code repackaging and distribution"}
                    "lucid-git"
                    {:input "test/documentation/lucid_git.clj"
                     :title "git"
                     :subtitle "wrapper for org.eclipse.jgit"}
                    "lucid-graph"
                    {:input "test/documentation/lucid_graph.clj"
                     :title "graph"
                     :subtitle "simple graph visualisations"}
                    #_"lucid-insight"
                    #_{:input "test/documentation/lucid_insight.clj"
                     :title "insight"
                     :subtitle "exploring functional connectivity"}
                    "lucid-mind"
                    {:input "test/documentation/lucid_mind.clj"
                     :title "mind"
                     :subtitle "contemplative reflection for the jvm"}
                    "lucid-package"
                    {:input "test/documentation/lucid_package.clj"
                     :title "package"
                     :subtitle "project packaging and dependencies"}
                    "lucid-publish"
                    {:input "test/documentation/lucid_publish.clj"
                     :title "publish"
                     :subtitle "generate documentation from code"}
                    "lucid-query"
                    {:input "test/documentation/lucid_query.clj"
                     :title "query"
                     :subtitle "intuitive search for code"}
                    "lucid-system"
                    {:input "test/documentation/lucid_system.clj"
                     :title "system"
                     :subtitle "system information and process monitoring"}
                    "lucid-unit"
                    {:input "test/documentation/lucid_unit.clj"
                     :title "unit"
                     :subtitle "metadata through unit tests"}}}
  
  :profiles {:dev {:dependencies [[compojure "1.6.0"]
                                  [ring "1.6.1"]
                                  [clj-http "3.6.0"]]}}
  
  :distribute {:jars  :dependencies
               :files [{:type :clojure
                        :levels 2
                        :path "src"
                        :standalone #{"aether" "distribute" "git" "graph" 
                                      "mind" "package" "publish" 
                                      "query" "system" "unit" "legacy"}}
                       {:subpackage "resources"
                        :path "resources"
                        :distribute {"publish" #{"theme"}}}]} 
  
  :java-source-paths ["java"]
  
  :jar-exclusions [#"^test\..+\.class"])
