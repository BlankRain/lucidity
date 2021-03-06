(ns lucid.distribute.analyser.java-test
  (:use hara.test)
  (:require [lucid.distribute.analyser.java :refer :all]
            [lucid.distribute.analyser :as analyser]
            [clojure.java.io :as io]))

^{:refer lucid.distribute.analyser.java/get-class :added "1.2"}
(fact "grabs the symbol of the class in the java file"
  (get-class
   (io/file "example/distribute.advance/java/im/chit/repack/common/Hello.java"))
  => 'im.chit.repack.common.Hello)

^{:refer lucid.distribute.analyser.java/get-imports :added "1.2"}
(fact "grabs the symbol of the class in the java file"
  (get-imports
   (io/file "example/distribute.advance/java/im/chit/repack/common/Hello.java"))
  => ()

  (get-imports
   (io/file "example/distribute.advance/java/im/chit/repack/web/Client.java"))
  => '(im.chit.repack.common.Hello))
