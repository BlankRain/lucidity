(ns lucid.legacy.match.java
  (:use [lucid.legacy.match.protocols]
        [lucid.legacy.match :only [match]]
        [clojure.string :only [lower-case]]))

(def ^:private method-name-pattern #"^(is|get)([A-Z].*)$")

(defn- dash-case 
  [^String s] 
  (let [gsub (fn [s re sub] (.replaceAll (re-matcher re s) sub))] 
    (-> s
      (gsub #"([A-Z]+)([A-Z][a-z])" "$1-$2")    
      (gsub #"([a-z]+)([A-Z])" "$1-$2")
      (lower-case)))) 

(defn- keywordize 
  [^String s]
  (let [[_ pre n] (re-find (re-matcher method-name-pattern s))]
    (-> n dash-case (str (if (= pre "is") "?")) keyword)))

(defmacro bean-match
  "Generate an implementation of match.core/IMatchLookup for a Java bean.
  Accessor method names are mapped to keys like this:
  
    isVisible       -> :visible?
    getText         -> :text
    getAbsolutePath -> :absolute-path 
    isFUD           -> :fud?
    getFUDFactor    -> :fud-factor

  "
  [class] 
  (let [method-names (->> (.getMethods ^Class (resolve class))
                       ; Methods that have is/get naming, no args and non-void return
                       (filter (fn [^java.lang.reflect.Method m] 
                                 (and (re-find method-name-pattern (.getName m))
                                      (= 0 (count (.getParameterTypes m)))
                                      (not= Void (.getReturnType m))))) 
                       ; Grab name as a symbol
                       (map    (fn [^java.lang.reflect.Method m] 
                                 (.getName m))))
        this (gensym "this")]
    `(extend-type ~class
       IMatchLookup
       (~'val-at [~this k# not-found#]
          (case k#
            ~@(mapcat 
                (fn [n] [(keywordize n) `(. ~this (~(symbol n)))]) 
                method-names)
            not-found#)))))
