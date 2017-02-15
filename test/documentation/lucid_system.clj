(ns documentation.lucid-system
  (:require [lucid.system :as system]))

"**lucid.system** is a wrapper around the [oshi](https://github.com/dblock/oshi) project, a free JNA-based (native) operating system information library for Java."

[[:chapter {:title "Installation"}]]

"Add to `project.clj` dependencies:

    [im.chit/lucid.system \"{{PROJECT.version}}\"]

**lucid.system** contain basic methods for class checking and predicates."

(comment
  (require '[lucid.system :as system]))

[[:chapter {:title "API"}]]

[[:section {:title "Hardware"}]]

[[:api {:namespace "lucid.system" :title "" 
        :only ["computer-system"
               "cpu"
               "displays"
               "fs"
               "memory"
               "network-ifs"
               "power"
               "sensors"
               "usb"]}]]

[[:section {:title "OS"}]]

[[:api {:namespace "lucid.system" :title "" 
        :only ["os"
               "all"]}]]

[[:section {:title "Process"}]]

[[:api {:namespace "lucid.system" :title "" 
        :only ["process-id"
               "process"
               "list-processes"]}]]
