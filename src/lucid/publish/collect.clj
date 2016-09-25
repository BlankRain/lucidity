(ns lucid.publish.collect
  (:require [hara.data.nested :as nested]))

(defn collect-namespaces
  "combines `:ns-form` directives into a namespace map for easy referral
   
   (collect-namespaces
    {:articles
     {\"example\"
      {:elements [{:type :ns-form
                   :ns    'clojure.core}]}}}
    \"example\")
   => '{:articles {\"example\" {:elements ()}}
        :namespaces {clojure.core {:type :ns-form :ns clojure.core}}}"
  {:added "1.2"}
  [{:keys [articles] :as interim} name]
  (let [all    (->> (get-in articles [name :elements])
                    (filter #(-> % :type (= :ns-form))))
        meta   (-> all first :meta)
        namespaces (->> all
                        (map (juxt :ns identity))
                        (into {}))]
    (-> interim
        (update-in [:articles name :meta] (fnil nested/merge-nested {}) meta)
        (update-in [:namespaces] (fnil nested/merge-nested {}) namespaces)
        (update-in [:articles name :elements]
                   (fn [elements] (filter #(-> % :type (not= :ns-form)) elements))))))

(defn collect-article
  "shunts `:article` directives into a seperate `:meta` section
   
   (collect-article
    {:articles {\"example\" {:elements [{:type :article
                                       :options {:color :light}}]}}}
    \"example\")
   => '{:articles {\"example\" {:elements []
                              :meta {:options {:color :light}}}}}"
  {:added "1.2"}
  [{:keys [articles] :as interim} name]
  (let [articles (->> (get-in articles [name :elements])
       (filter #(-> % :type (= :article)))
       (apply nested/merge-nested {}))]
    (-> interim
        (update-in [:articles name :meta] (fnil nested/merge-nested {}) (dissoc articles :type))
        (update-in [:articles name :elements]
                   (fn [elements] (filter #(-> % :type (not= :article)) elements))))))

(defn collect-global
  "shunts `:global` directives into a globally available `:meta` section
   
   (collect-global
    {:articles {\"example\" {:elements [{:type :global
                                       :options {:color :light}}]}}}
    \"example\")
   => {:articles {\"example\" {:elements ()}}
       :meta {:options {:color :light}}}"
  {:added "1.2"}
  [{:keys [articles] :as interim} name]
  (let [global (->> (get-in articles [name :elements])
                    (filter #(-> % :type (= :global)))
                    (apply nested/merge-nested {}))]
    (-> interim
        (update-in [:global] (fnil nested/merge-nested {}) (dissoc global :type))
        (update-in [:articles name :elements]
                   (fn [elements] (filter #(-> % :type (not= :global)) elements))))))

(defn collect-tags
  "puts any element with `:tag` attribute into a seperate `:tag` set
   
   (collect-tags
    {:articles {\"example\" {:elements [{:type :chapter :tag  \"hello\"}
                                      {:type :chapter :tag  \"world\"}]}}}
    \"example\")
   => {:articles {\"example\" {:elements [{:type :chapter :tag \"hello\"}
                                        {:type :chapter :tag \"world\"}]
                             :tags #{\"hello\" \"world\"}}}}"
  {:added "1.2"}
  [{:keys [articles] :as interim} name]
  (->> (get-in articles [name :elements])
       (reduce (fn [m {:keys [tag] :as ele}]
                                (cond (nil? tag) m

                                      (get m tag) (do (println "There is already an existing tag for" ele)
                                                      m)
                                      :else (conj m tag)))
               #{})
       (assoc-in interim [:articles name :tags])))

(defn collect-citations
  "shunts `:citation` directives into a seperate `:citation` section
   
   (collect-citations
    {:articles {\"example\" {:elements [{:type :citation :author \"Chris\"}]}}}
    \"example\")
   => {:articles {\"example\" {:elements [],
                             :citations [{:type :citation, :author \"Chris\"}]}}}"
  {:added "1.2"}
  [{:keys [articles] :as interim} name]
  (let [citations (->> (get-in articles [name :elements])
                       (filter #(-> % :type (= :citation))))]
    (-> interim
        (assoc-in  [:articles name :citations] citations)
        (update-in [:articles name :elements]
                   (fn [elements] (filter #(-> % :type (not= :citation)) elements))))))
