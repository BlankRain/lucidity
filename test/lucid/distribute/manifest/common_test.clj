(ns lucid.distribute.manifest.common-test
  (:use hara.test)
  (:require [lucid.distribute.manifest.common :refer :all]
            [lucid.distribute.analyser]))

^{:refer lucid.distribute.manifest.common/section-vec :added "1.2"}
(fact "creates a vector of section strings"

  (section-vec "hello/world/clojure.clj")
  => ["hello" "world" "clojure.clj"])

^{:refer lucid.distribute.manifest.common/drop-matching :added "1.2"}
(fact "drop all matching elements in two arrays"

  (drop-matching [1 2 3 4]
                 [1 2 4 6])
  => [[3 4] [4 6]])

^{:refer lucid.distribute.manifest.common/best-match :added "1.2"}
(fact "provides the best match for a file and a distribution map"

  (best-match "stuff/x.edn"
              {"common" #{"common"}
               "web"    #{"web"}})
  => [nil 0]
  
  (best-match "web/b.html"
              {"common" #{"common"}
               "web"    #{"web"}})
  => ["web" 1])

^{:refer lucid.distribute.manifest.common/group-distribution :added "1.2"}
(fact "allows for organisation of multiple files within a distribution"
  
  (group-distribution {"common" #{"common"}
                       "web"    #{"web"}}
                      ["common/a.txt" "common/b.txt"
                       "stuff/x.edn" "stuff/y.edn"
                       "web/a.html" "web/b.html"])
  => {nil #{"stuff/y.edn" "stuff/x.edn"}
      "common" #{"common/a.txt" "common/b.txt"}
      "web" #{"web/b.html" "web/a.html"}})

^{:refer lucid.distribute.manifest.common/group-include :added "1.2"}
(fact "allows for including only patterns in files"

  (group-include ["foo" "bar"]
                 {nil #{"stuff/x.edn"
                        "foo/y.edn"
                        "bar/z.edn"}})
  => {nil ["foo/y.edn" "bar/z.edn"]})

^{:refer lucid.distribute.manifest.common/build-distribution :added "1.2"}
(fact "constructs a distribution for filemap to occur"

  (build-distribution "example/distribute.advance"
                      {:subpackage "resources"
                       :path "resources"
                       :distribute {"common" #{"common"}
                                    "web"    #{"web"}}
                       :dependents #{"core"}})
  => {nil #{"stuff/y.edn" "stuff/x.edn"},
      "common" #{"common/a.txt" "common/b.txt"},
      "web" #{"web/b.html" "web/a.html"}})

^{:refer lucid.distribute.manifest.common/create-filemap :added "1.2"}
(fact "creates a filemap for a list of files"

  (create-filemap {nil #{"stuff/y.edn" "stuff/x.edn"}
                   "common" #{"common/a.txt" "common/b.txt"}
                   "web" #{"web/b.html" "web/a.html"}}
                  {:root   "resources"
                   :folder "resources"
                   :pnil   "resources"})
  => (contains {"common"    anything  ;; {resources/common/b.txt resources/common/a.txt},
                "resources"  anything  ;; {resources/stuff/y.edn resources/stuff/x.edn},
                "web"        anything  ;; {resources/web/a.html resources/web/b.html}
                }))

^{:refer lucid.distribute.manifest.common/build-filemap :added "1.2"}
(fact "builds manifest for resources and java folder"

  (build-filemap "example/repack.advance"
                 {:subpackage "resources"
                  :path "resources"
                  :distribute {"common" #{"common"}
                               "web"    #{"web"}}
                  :dependents #{"core"}})
  => (contains {"common"    anything  ;; {resources/common/b.txt resources/common/a.txt},
                "resources"  anything  ;; {resources/stuff/y.edn resources/stuff/x.edn},
                "web"        anything  ;; {resources/web/a.html resources/web/b.html}
                })

  (build-filemap "example/repack.advance"
                  {:subpackage "jvm"
                   :path "java/im/chit/repack"
                   :distribute {"common" #{"common"}
                                "web"    #{"web"}}
                   :dependents #{"core"}})
  => (contains {"common"     anything  ;; {java/im/chit/repack/common/Hello.java},
                "jvm"        anything  ;; {java/im/chit/repack/native/Utils.java},
                "web"        anything  ;; {java/im/chit/repack/web/Client.java}
                }))

(comment
  
  (require 'lucid.unit)
  (lucid.unit/import))
