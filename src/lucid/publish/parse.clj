(ns lucid.publish.parse
  (:require [lucid.query :as query]
            [lucid.publish.parse.checks :as checks]
            [clojure.java.io :as io]
            [rewrite-clj.zip :as source]
            [rewrite-clj.node :as node]))

(def ^:dynamic *spacing* 2)
(def ^:dynamic *indentation* 0)
(def ^:dynamic *namespace* nil)

(declare parse-file parse-facts-form)

(defn parse-ns-form
  "converts a ns zipper into an element
 
   (-> (z/of-string \"(ns example.core)\")
       (parse-ns-form))
   => '{:type :ns-form
        :indentation 0
        :ns example.core
        :code \"(ns example.core)\"}"
  {:added "1.2"}
  [zloc]
  {:type :ns-form
   :indentation *indentation*
   :ns   (-> zloc source/sexpr second)
   :code (source/string zloc)
   :meta (if (-> zloc source/tag (= :meta))
           (-> (source/node zloc)
               :children
               first
               node/sexpr)
           {})})

(defn code-form
  "converts a form zipper into a code string
   
   (-> (z/of-string \"(fact (+ 1 1) \\n => 2)\")
       (code-form 'fact))
   => \"(+ 1 1) \\n  => 2\""
  {:added "1.2"}
  [zloc symbol]
  (let [s (source/string zloc)]
    (-> (.substring s 1 (dec (.length s)))
        (.replaceFirst (str symbol "(\\s+)?") ""))))

(defn parse-fact-form
  "convert a fact zipper into an element
 
   (-> (z/of-string \"(fact (+ 1 1) \\n => 2)\")
       (parse-fact-form))
   => {:type :block :indentation 2 :code \"(+ 1 1) \\n => 2\"}"
  {:added "1.2"}
  [zloc]
  {:type :test
   :indentation (+ *indentation* *spacing*)
   :code (code-form zloc "fact")})

(defn parse-facts-form
  ""
  [zloc]
  {:type :test
   :indentation (+ *indentation* *spacing*)
   :code (code-form zloc "facts")})

(defn parse-comment-form
  "convert a comment zipper into an element
 
   (-> (z/of-string \"(comment (+ 1 1) \\n => 2)\")
       (parse-comment-form))
   => {:type :block :indentation 2 :code \"(+ 1 1) \\n => 2\"}"
  {:added "1.2"}
  [zloc]
  {:type :block
   :indentation (+ *indentation* *spacing*)
   :code (code-form zloc "comment")})

(defn is-code-form
  ""
  [zloc]
  (let [zloc* (-> zloc source/down source/right)]
    (cond (query/match zloc* '(= _ _))
          (let [first  (-> zloc* source/down source/right)
                second (-> first source/right)]
            (str (source/->string first)
                 "\n=> "
                 (source/->string second)))

          :else
          (code-form zloc "is"))))

(defn parse-is-form
  "" [zloc]
  {:type :block
   :indentation (+ *indentation* *spacing*)
   :code (is-code-form zloc)})

(defn parse-deftest-form
  "" [zloc]
  {:type :test
   :source (meta (source/node zloc))})

(defn parse-paragraph
  "converts a string zipper into an element
 
   (-> (z/of-string \"\\\"this is a paragraph\\\"\")
       (parse-paragraph))
   => {:type :paragraph :text \"this is a paragraph\"}"
  {:added "1.2"}
  [zloc]
  {:type :paragraph
   :text (source/sexpr zloc)})

(defn parse-directive
  "converts a directive zipper into an element
 
   (-> (z/of-string \"[[:chapter {:title \\\"hello world\\\"}]]\")
       (parse-directive))
   => {:type :chapter :title \"hello world\"}
 
   (binding [*namespace* 'example.core]
     (-> (z/of-string \"[[:ns {:title \\\"hello world\\\"}]]\")
         (parse-directive)))
   => {:type :ns, :title \"hello world\", :ns 'example.core}"
  {:added "1.2"}
  [zloc]
  (let [tloc       (-> zloc source/down source/down)
        tag        (-> tloc source/sexpr)
        attributes (-> tloc source/right source/sexpr)
        directive  (merge {:type tag} attributes)]
    (if (= :ns tag)
      (assoc directive :ns *namespace*)
      directive)))

(defn parse-attribute
  "coverts an attribute zipper into an element
 
   (-> (z/of-string \"[[{:title \\\"hello world\\\"}]]\")
       (parse-attribute))
   => {:type :attribute, :title \"hello world\"}"
  {:added "1.2"}
  [zloc]
  (let [attributes (-> zloc source/down source/down source/sexpr)]
    (assoc attributes :type :attribute)))

(defn parse-code-directive
  "coverts an code directive zipper into an element
 
   (-> (z/of-string \"[[:code {:language :ruby} \\\"1 + 1 == 2\\\"]]\")
       (parse-code-directive))
   => {:type :block, :indentation 0 :code \"1 + 1 == 2\" :language :ruby}"
  {:added "1.2"}
  [zloc]
  (-> (parse-directive zloc)
      (assoc :type :block
             :indentation *indentation*
             :code (-> zloc
                       source/down
                       source/down
                       source/right
                       source/right
                       source/sexpr))))

(defn parse-whitespace
  "coverts a whitespace zipper into an element
 
   (-> (z/of-string \"1 2 3\")
       (z/right*)
       (parse-whitespace))
   => {:type :whitespace, :code [\" \"]}"
  {:added "1.2"}
  [zloc]
  {:type :whitespace
   :code [(node/string (source/node zloc))]})

(defn parse-code
  "coverts a code zipper into an element
 
   (-> (z/of-string \"(+ 1 1) (+ 2 2)\")
       (parse-code))
   => {:type :code, :indentation 0, :code [\"(+ 1 1)\"]}"
  {:added "1.2"}
  [zloc]
  {:type :code
   :indentation *indentation*
   :code [(node/string (source/node zloc))]})

(defn wrap-meta
  "if form is meta, then go down a level"
  {:added "1.2"}
  [f]
  (fn [zloc]
    (if (= :meta (source/tag zloc))
      (f (-> zloc source/down source/right))
      (f zloc))))

(defn wrap-line-info
  ""
  [f]
  (fn [zloc]
    (assoc (f zloc) :line (meta (source/node zloc)))))

(defn parse-single
  "parse a single zloc"
  {:added "1.2"}
  [zloc]
  (cond (checks/whitespace? zloc)
        (parse-whitespace zloc)

        (checks/ns? zloc)
        (parse-ns-form zloc)

        (checks/is? zloc)
        ((wrap-meta parse-is-form) zloc)

        (checks/deftest? zloc)
        ((wrap-meta parse-deftest-form) zloc)

        (checks/fact? zloc)
        ((wrap-meta parse-fact-form) zloc)

        (checks/facts? zloc)
        ((wrap-meta parse-facts-form) zloc)

        (checks/comment? zloc)
        ((wrap-meta parse-comment-form) zloc)

        (checks/directive? zloc)
        ((wrap-meta parse-directive) zloc)

        (checks/attribute? zloc)
        ((wrap-meta parse-attribute) zloc)

        (checks/code-directive? zloc)
        ((wrap-meta parse-code-directive) zloc)

        (checks/paragraph? zloc)
        (parse-paragraph zloc)

        :else (parse-code zloc)))

(declare parse-loop)

(defn merge-current
  "if not whitespace, then merge output"
  {:added "1.2"}
  [output current]
  (cond (nil? current) output

        (= :whitespace (:type current)) output

        :else (conj output current)))

(defn parse-inner
  "parses the inner form of the fact and comment function"
  {:added "1.2"}
  [zloc f output current opts]
  (let [sub (binding [*indentation* (+ *indentation* *spacing*)]
              (parse-loop (f zloc) opts))]
    (apply conj (merge-current output current) sub)))

(defn parse-loop
  "the main loop for the parser
 
   (-> (z/of-string \"(ns example.core) 
                     [[:chapter {:title \\\"hello\\\"}]] 
                     (+ 1 1) 
                     (+ 2 2)\")
       (parse-loop {}))
   => [{:type :ns-form, :indentation 0, :ns 'example.core, :code \"(ns example.core)\"}
      {:type :chapter, :title \"hello\"}
       {:type :code, :indentation 0, :code [\"(+ 1 1)\"
                                            \" \"
                                            \"\\n\"
                                            \"                    \"
                                            \"(+ 2 2)\"]}]"
  {:added "1.2"}
  ([zloc opts] (parse-loop zloc opts nil []))
  ([zloc opts current output]
   (cond (nil? zloc)
         (merge-current output current)

         :else
         (let [element ((wrap-line-info parse-single) zloc)]
           (cond (= (:type current) :attribute)
                 (if (not= :whitespace (:type element))
                   (recur (source/right* zloc) opts (merge current element) output)
                   (recur (source/right* zloc) opts current output))

                 (nil? (:type element))
                 (throw (Exception. (str "element should have a type " element (:type element))))

                 (and (#{:code :whitespace} (:type element))
                      (= (:type current) :code))
                 (recur (source/right* zloc)
                        opts
                        (update-in current [:code] #(apply conj % (:code element)))
                        output)

                 :else
                 (case (:type element)
                   :ns-form    (binding [*namespace* (:ns element)]
                                 (parse-loop (source/right* zloc)
                                             opts
                                             element
                                             (merge-current output current)))
                   :attribute  (recur (source/right* zloc) opts element
                                      (merge-current output current))
                   :file       (recur (source/right* zloc) opts nil
                                      (apply conj output (parse-file (:src element) opts)))
                   :facts      (recur (source/right* zloc) opts nil
                                      (parse-inner zloc
                                                   #(-> % source/down source/right)
                                                   output
                                                   current
                                                   opts))
                   :deftest    (recur (source/right* zloc) opts nil
                                      (parse-inner zloc
                                                   #(-> % source/down source/right source/right)
                                                   output current
                                                   opts))
                   (recur (source/right* zloc) opts element (merge-current output current))))))))

(defn parse-file
  "parse the entire file"
  {:added "1.2"}
  [file opts]
  (let [path (if (:root opts)
               (str (:root opts) "/" file)
               file)]
    (with-open [input (io/input-stream path)]
      (parse-loop (source/of-string (slurp path)) opts))))
