(ns lucid.distribute.manifest.common
  (:require [hara.io.file :as fs]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [lucid.distribute.analyser.base :as analyser]
            [lucid.distribute.common :as common]))

(defn section-vec
  "creates a vector of section strings
 
   (section-vec \"hello/world/clojure.clj\")
   => [\"hello\" \"world\" \"clojure.clj\"]"
  {:added "1.2"}
  [path]
  (->> (fs/section path)
       (.iterator)
       (iterator-seq)
       (mapv str)))

(defn drop-matching
  "drop all matching elements in two arrays
 
   (drop-matching [1 2 3 4]
                  [1 2 4 6])
   => [[3 4] [4 6]]"
  {:added "1.2"}
  [u v]
  (cond (or (empty? u) (empty? v)) [u v]

        (= (first u) (first v))
        (recur (rest u) (rest v))

        :else [u v]))

(defn best-match
  "provides the best match for a file and a distribution map
 
   (best-match \"stuff/x.edn\"
               {\"common\" #{\"common\"}
                \"web\"    #{\"web\"}})
   => [nil 0]
   
   (best-match \"web/b.html\"
               {\"common\" #{\"common\"}
                \"web\"    #{\"web\"}})
   => [\"web\" 1]"
  {:added "1.2"}
  [file distribution]
  (let [fvec   (section-vec file)]
    (reduce-kv (fn [i k v]
                 (reduce (fn [[sym cnt] ele]
                           (let [tcnt (->> (section-vec ele)
                                           (drop-matching fvec)
                                           (first)
                                           (count)
                                           (- (count fvec)))]
                             (if (> tcnt cnt)
                               [k tcnt]
                               [sym cnt])))
                         i v))
               [nil 0]
               distribution)))

(defn group-distribution
  "allows for organisation of multiple files within a distribution
   
   (group-distribution {\"common\" #{\"common\"}
                        \"web\"    #{\"web\"}}
                       [\"common/a.txt\" \"common/b.txt\"
                        \"stuff/x.edn\" \"stuff/y.edn\"
                        \"web/a.html\" \"web/b.html\"])
   => {nil #{\"stuff/y.edn\" \"stuff/x.edn\"}
       \"common\" #{\"common/a.txt\" \"common/b.txt\"}
       \"web\" #{\"web/b.html\" \"web/a.html\"}}"
  {:added "1.2"}
  [distribution files]
  (reduce (fn [i f]
            (let [[sym _] (best-match f distribution)]
              (update-in i [sym] (fnil #(conj % f) #{f}))))
          {}
          files))

(defn group-include
  "allows for including only patterns in files
 
   (group-include [\"foo\" \"bar\"]
                  {nil #{\"stuff/x.edn\"
                         \"foo/y.edn\"
                         \"bar/z.edn\"}})
   => {nil [\"foo/y.edn\" \"bar/z.edn\"]}"
  {:added "1.2"}
  [includes group]
  (if includes
    (let [files (->> (get group nil)
                     (filterv (fn [file]
                                (first (best-match file {:_ includes})))))]
      (assoc group nil files))
    group))

(defn create-filemap
  "creates a filemap for a list of files
 
   (create-filemap {nil #{\"stuff/y.edn\" \"stuff/x.edn\"}
                    \"common\" #{\"common/a.txt\" \"common/b.txt\"}
                    \"web\" #{\"web/b.html\" \"web/a.html\"}}
                   {:root   \"resources\"
                    :folder \"resources\"
                    :pnil   \"resources\"})
  => (contains {\"common\"    anything  ;; {resources/common/b.txt resources/common/a.txt},
                 \"resources\"  anything  ;; {resources/stuff/y.edn resources/stuff/x.edn},
                 \"web\"        anything  ;; {resources/web/a.html resources/web/b.html}
                 })"
  {:added "1.2"}
  ([files] (create-filemap files {:pnil "default"}))
  ([files {:keys [pnil root folder] :as opts
           :or   {folder "."
                  root   ""}}]
     (reduce-kv (fn [m k v]
                  (let [grp (or k pnil)]
                    (->> v
                         (map (fn [ele]
                                (let [fele   (.toFile (fs/path root folder ele))
                                      finfo  (analyser/file-info fele)]
                                  (-> finfo
                                      (assoc :type (fs/file-type fele)
                                             :path (str (fs/relativize root fele)))
                                      (common/map->FileInfo)))))
                         (set)
                         (assoc m grp))))
                {}
                files)))

(defn build-distribution
  "constructs a distribution for filemap to occur
 
   (build-distribution \"example/distribute.advance\"
                       {:subpackage \"resources\"
                        :path \"resources\"
                        :distribute {\"common\" #{\"common\"}
                                     \"web\"    #{\"web\"}}
                        :dependents #{\"core\"}})
  => {nil #{\"stuff/y.edn\" \"stuff/x.edn\"},
       \"common\" #{\"common/a.txt\" \"common/b.txt\"},
       \"web\" #{\"web/b.html\" \"web/a.html\"}}"
  {:added "1.2"}
  [root {:keys [path subpackage distribute include] :as cfg}]
  (let [res-folder (io/file root path)
        distro     (->> (file-seq res-folder)
                        (filter #(not (.isDirectory %)))
                        (filter (fn [f] (not (some #(re-find % (.getPath f)) 
                                                   (:jar-exclusions cfg)))))
                        (map #(str (fs/relativize res-folder %)))
                        (group-distribution distribute)
                        (group-include include))]
    distro))

(defmulti build-filemap
  "builds manifest for resources and java folder
 
   (build-filemap \"example/repack.advance\"
                  {:subpackage \"resources\"
                   :path \"resources\"
                   :distribute {\"common\" #{\"common\"}
                                \"web\"    #{\"web\"}}
                   :dependents #{\"core\"}})
   => (contains {\"common\"    anything  ;; {resources/common/b.txt resources/common/a.txt},
                 \"resources\"  anything  ;; {resources/stuff/y.edn resources/stuff/x.edn},
                 \"web\"        anything  ;; {resources/web/a.html resources/web/b.html}
                 })
 
   (build-filemap \"example/repack.advance\"
                   {:subpackage \"jvm\"
                    :path \"java/im/chit/repack\"
                   :distribute {\"common\" #{\"common\"}
                                 \"web\"    #{\"web\"}}
                    :dependents #{\"core\"}})
   => (contains {\"common\"     anything  ;; {java/im/chit/repack/common/Hello.java},
                 \"jvm\"        anything  ;; {java/im/chit/repack/native/Utils.java},
                 \"web\"        anything  ;; {java/im/chit/repack/web/Client.java}
                 })"
  {:added "1.2"}
  (fn [project-dir cfg] (:type cfg)))

(defmethod build-filemap :default
  [root {:keys [path subpackage] :as cfg}]
  (-> (build-distribution root cfg)
      (create-filemap  {:root   root 
                        :folder path
                        :pnil   subpackage})))
