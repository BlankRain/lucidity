(ns lucid.system.jvm
  (:require [hara.object :as object])
  (:import [java.lang.management ManagementFactory ClassLoadingMXBean CompilationMXBean
            GarbageCollectorMXBean MemoryMXBean MemoryPoolMXBean MemoryManagerMXBean 
            OperatingSystemMXBean RuntimeMXBean ThreadMXBean MemoryUsage]))

(object/map-like
 
 ClassLoadingMXBean
 {:tag "class-loading"}
 CompilationMXBean
 {:tag "compilation"}
 GarbageCollectorMXBean
 {:tag "gc"}
 MemoryMXBean
 {:tag "memory"}
 MemoryPoolMXBean
 {:tag "memory-pool"
  :exclude [:collection-usage-threshold-exceeded?
            :collection-usage-threshold-count
            :collection-usage-threshold
            :collection-usage
            :usage-threshold
            :usage-threshold-count
            :usage-threshold-exceeded?]}
 MemoryManagerMXBean
 {:tag "memory-manager"}
 OperatingSystemMXBean
 {:tag "os"}
 RuntimeMXBean
 {:tag "runtime"}
 ThreadMXBean
 {:tag "thread"}
 MemoryUsage
 {:tag "usage"})

(defn class-loading-bean []
  (ManagementFactory/getClassLoadingMXBean))

(defn compilation-bean []
  (ManagementFactory/getCompilationMXBean))

(defn gc-bean []
  (ManagementFactory/getGarbageCollectorMXBeans))

(defn memory-bean []
  (ManagementFactory/getMemoryMXBean))

(defn memory-manager-bean []
  (ManagementFactory/getMemoryManagerMXBeans))

(defn memory-pool-bean []
  (ManagementFactory/getMemoryPoolMXBeans))

(defn os-bean []
  (ManagementFactory/getOperatingSystemMXBean))

(defn runtime-bean []
  (ManagementFactory/getThreadMXBean))

(defn thread-bean []
  (ManagementFactory/getThreadMXBean))

(def jvm-map
  {:class-loading class-loading-bean
   :compilation compilation-bean
   :gc gc-bean
   :memory memory-bean
   :memory-manager memory-manager-bean
   :memory-pool memory-pool-bean
   :os os-bean
   :runtime runtime-bean
   :thread thread-bean})
