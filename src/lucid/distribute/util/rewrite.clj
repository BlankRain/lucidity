(ns lucid.distribute.util.rewrite
  (:require [rewrite-clj.zip :as z]))

(def ^:dynamic *sep* (System/getProperty "file.separator"))

(defn project-zip
  "returns the zipper for a project file"
  {:added "1.2"}
  [project]
  (-> (z/of-string (slurp (str (:root project) *sep* "project.clj")))
      (z/find-value z/next 'defproject)))

(defn replace-project-value
  "replaces the value in a project for a particular keys"
  {:added "1.2"}
  [zipper key value]
  (if-let [pos (-> zipper
                   (z/find-value key))]
    (-> pos
        (z/right)
        (z/replace value)
        (z/up)
        (z/find-value z/next 'defproject))
    zipper))

(defn update-project-value
  ""
  [zipper key f]
  (if-let [pos (-> zipper
                   (z/find-value key))]
    (let [pos (z/right pos)
          val (z/sexpr pos)]
      (-> pos
          (z/replace (f val))
          (z/up)
          (z/find-value z/next 'defproject)))
    zipper))

(defn remove-project-key
  "replaces the value in a project for a particular keys"
  {:added "1.2"}
  [zipper key]
  (if-let [pos (-> zipper
                   (z/find-value key))]
    (-> pos
        (z/remove*)
        (z/right)
        (z/remove*)
        (z/up)
        (z/find-value z/next 'defproject))
    zipper))

(defn add-project-key
  "replaces the value in a project for a particular keys"
  {:added "1.2"}
  [zipper key value]
  (if-let [pos (-> zipper
                   (z/find-value key))]
    (-> pos
        (z/remove*)
        (z/right)
        (z/remove*)
        (z/up)
        (z/find-value z/next 'defproject))
    (-> zipper
        (z/find-value z/next 'defproject)
        (z/rightmost)
        (z/insert-right value)
        (z/insert-right key)
        (z/up)
        (z/find-value z/next 'defproject))))

(defn root-project-string
  "generates the `project.clj` for the root project"
  {:added "1.2"}
  [project manifest]
  (-> (project-zip project)
      (update-project-value :dependencies
                            (fn [x] (->> manifest :root :dependencies (vec))))
      ;;(add-project-key :scm (:scm project))
      (remove-project-key :profiles)
      (remove-project-key :distribute)
      z/print-root
      with-out-str))

(defn branch-project-string
  "generates `project.clj` for the branch projects"
  {:added "1.2"}
  [project manifest name]
  (-> (project-zip project)
      (update-project-value 'defproject
                            (fn [x] (symbol (str (:group project) "/"
                                                 (:artifact project) "." name))))
      ;;(add-project-key :scm (:scm project))
      (update-project-value :description
                            (fn [x] (or (-> manifest :branches (get name) :description) x)))
      (replace-project-value :dependencies
                             (-> manifest :branches (get name) :dependencies))
      (remove-project-key :profiles)
      (remove-project-key :distribute)
      z/print-root
      with-out-str))
