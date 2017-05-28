(ns lucid.aether.authentication
  (:require [hara.object :as object]
            [hara.reflect :as reflect]
            [hara.object.write :as write])
  (:import (org.eclipse.aether.repository Authentication)
           (org.eclipse.aether.util.repository AuthenticationBuilder
                                               ChainedAuthentication
                                               StringAuthentication
                                               SecretAuthentication)))

(object/vector-like

 StringAuthentication
 {:tag "auth.string"
  :read (fn [auth]
          [(keyword (reflect/apply-element auth "key" []))
           (reflect/apply-element auth "value" [])])
  :write (fn [[k v]]
           ((reflect/query-class StringAuthentication ["new" :#]) (name k) v))}
 
 SecretAuthentication
 {:tag "auth.secret"
  :read (fn [auth]
          [(keyword (reflect/apply-element auth "key" []))
           (->> (reflect/apply-element auth "value" [])
                (vector)
                (reflect/apply-element auth "xor")
                (apply str))])
  :write (fn [[k v]]
           ((reflect/query-class SecretAuthentication ["new" :#])
            (name k)
            v))})

(defn auth-map
  [auth]
  (->> (seq (reflect/apply-element auth "authentications" []))
       (map object/to-data)
       (into {})))

(object/map-like
 ChainedAuthentication
 {:tag "auth.chained"
  :read {:to-map auth-map}
  :write {:from-map
          (fn [m]
            (.build (object/from-data m AuthenticationBuilder)))}}


 Authentication
 {:tag "auth"
  :write {:from-map
          (fn [m]
            (.build (object/from-data m AuthenticationBuilder)))}}

 AuthenticationBuilder
 {:tag "builder.auth"
  :read {:to-map auth-map}
  :write {:empty (fn [_] (AuthenticationBuilder.))
          :methods (object/write-all-setters AuthenticationBuilder {:prefix "add"})}})  
