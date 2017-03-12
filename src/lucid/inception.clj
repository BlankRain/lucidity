(ns lucid.inception
  (:import net.bytebuddy.ByteBuddy))

(-> (ByteBuddy.)
    (.subclass Object)
    (.name "example.Type")
    (.make)
    (.load (.getContextClassLoader (Thread/currentThread)))
    (.getLoaded)
    (.newInstance)
    (str))
(.make)
(.load )


(comment
  (gen-interface )

  (./pull '[cloud.orbit/orbit-runtime "0.9.17"])
  [[cloud.orbit/orbit-commons "0.9.17"] [cloud.orbit/orbit-core "0.9.17"] [cloud.orbit/orbit-infinispan-cluster "0.9.17"] [cloud.orbit/orbit-runtime "0.9.17"] [com.ea.agentloader/ea-agent-loader "1.0.2"] [com.ea.async/ea-async "1.0.5"] [javax.inject/javax.inject "1"] [org.infinispan/infinispan-embedded "8.1.4.Final"] [org.jboss.spec.javax.transaction/jboss-transaction-api_1.1_spec "1.0.1.Final"] [org.slf4j/slf4j-api "1.7.21"]]

  (import [cloud.orbit.actors Actor Stage Stage$Builder]
          [cloud.orbit.actors.runtime AbstractActor]
          [cloud.orbit.concurrent Task]) 

  (-> (Stage$Builder.) 
      (.clusterName "orbit-hello-world")
      (.build))
  (def stg *1)

  (.join (.start stg))

  (.bind stg)
  
  (gen-interface
   :name example.Hello
   :extends [cloud.orbit.actors.Actor]
   :methods [[sayHello [String] cloud.orbit.concurrent.Task]])

  (load-string
   (gen-class
    :name example.HelloActor
    :extends [cloud.orbit.actors.runtime.AbstractActor]
    :implements [example.Hello]
    :methods [[sayHello [String] cloud.orbit.concurrent.Task]]))

  (compile )
  
  example.HelloActor
  
  (.? lucid.inception.Hello)

  
  (proxy [AbstractActor lucid.inception.Hello]
      (sayHello [greeting]))
  )
