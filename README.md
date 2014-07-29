# vinyasa

[Give your clojure workflow more flow](http://z.caudate.me/give-your-clojure-workflow-more-flow/)

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**

- [vinyasa](#vinyasa)
	- [Whats New](#whats-new)
			- [0.2.0](#020)
			- [0.1.9](#019)
			- [0.1.8](#018)
	- [Installation](#installation)
	- [Quickstart:](#quickstart)
		- [pull](#pull)
		- [lein](#lein)
		- [reimport](#reimport)
		- [graft](#graft)
		- [inject](#inject)
		- [inject - installation](#inject---installation)
	- [License](#license)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

## Whats New

#### 0.2.1

Added new `graft` functionality from [here](https://github.com/zcaudate/vinyasa/issues/8)

```clojure
[im.chit/vinyasa.graft "0.2.1"]
```

#### 0.2.0

vinyasa has now been [repackaged](https://github.com/zcaudate/lein-repack). Functionality can now be accessed via seperate dependencies:

```clojure
[im.chit/vinyasa.inject "VERSION"]
[im.chit/vinyasa.pull "VERSION"]
[im.chit/vinyasa.lein "VERSION"]
[im.chit/vinyasa.reimport "VERSION"]
```

Or all of them together:

```clojure
[im.chit/vinyasa "VERSION"]
```

#### 0.1.9
Changed `vinyasa.lein` according to [comments](http://z.caudate.me/clojure-dynamic-languages-creativity-and-simplicity/) on blog.

WARNING: There are [issues](https://github.com/zcaudate/vinyasa/issues/3) with adding leiningen as a dependency. It should be disabled if it causes problems.

#### 0.1.8
Breaking changes to `reimport`. Now reimport is used like this:

```clojure
(reimport :all)  ;; compile import all symbols into namespace

(reimport 'com.example.Util
          '[net.example Hello World]
          false) ;; do not import symbols
```
## Installation

Add `vinyasa` to your `profiles.clj` (located in `~/.lein/profiles.clj`) as well as your version of leiningen. Please note the issue with `vinyasa.lein` with light table as well as other libraries. You may need to disable `vinyasa.lein` and `leiningen` if there are problems.

```clojure
{:user {:plugins [...]
        :dependencies [....
                       [leiningen #=(leiningen.core.main/leiningen-version)]
                       [im.chit/vinyasa "0.2.1"]
                       ....]
        ....}
        :injections [...
                     (require '[vinyasa.inject :as inj])
                     (inj/inject 'clojure.core '>
                       '[[vinyasa.inject inject]
                         [vinyasa.pull pull]
                         [vinyasa.graft graft]
                         [vinyasa.lein lein]
                         [vinyasa.reimport reimport]])
                     ...]
      }
```

*NOTE* Its very important that `leiningen` is in your dependencies as `lein` and `reimport` have dependencies on leiningen functions

## Quickstart:

Once `profiles.clj` is installed, run `lein repl`.

```clojure
> (lein)    ;; => entry point to leiningen
> (reimport) ;; => dynamically reloads *.java files
> (pull 'hiccup) ;; => pull repositories from clojars
> (inject 'clojure.core '[[hiccup.core html]]) ;; => injects new methods into clojure.core
> (html [:p "Hello World"]) ;; => injected method
;;=> "<p>hello world</p>"
```

### pull

How many times have you forgotten a library dependency for `project.clj` and then had to restart your nrepl? `pull` is a convienient wrapper around the `pomegranate` library:

```clojure
> (require 'hiccup.core)
;; => java.io.FileNotFoundException: Could not locate hiccup/core__init.class or hiccup/core.clj on classpath:

> (require 'hiccup.core)
> (pull 'hiccup)
;; => {[org.clojure/clojure "1.2.1"] nil,
;;     [hiccup "1.0.4"] #{[org.clojure/clojure "1.2.1"]}}

> (use 'hiccup.core)
> (html [:p "hello World"])
;; => "<p>hello World</p>"

> (pull 'hiccup "1.0.1")
;; => {[org.clojure/clojure "1.2.1"] nil,
;;     [hiccup "1.0.1"] #{[org.clojure/clojure "1.2.1"]}}
```
### lein

Don't you wish that you had the power of leiningen within the repl itself? `lein` is that entry point. You don't have to open up another terminal window anymore, You can now run your commands in the repl!

```clojure
> (lein)
;; Leiningen is a tool for working with Clojure projects.
;;
;; Several tasks are available:
;; check               Check syntax and warn on reflection.
;; classpath           Write the classpath of the current project to output-file.
;; clean               Remove all files from paths in project's clean-targets.
;; cljsbuild           Compile ClojureScript source into a JavaScript file.
;;
;;  .....
;;  .....

> (lein install)     ;; Install to local maven repo

> (lein uberjar)     ;; Create a jar-file

> (lein push)        ;; Deploy on clojars (I am using lein-clojars plugin)

> (lein javac)       ;; Compile java classes (use vinyasa.reimport instead)

```

### reimport

Don't you wish that you could make some changes to your java files and have them instantly loaded into your repl without restarting? Well now you can!

For example, in project.clj, you have specified your `:java-source-paths`

```clojure
(defproject .....
   :source-paths ["src/clojure"]
   :java-source-paths ["src/java"]
   :java-test-paths ["test/java"]    ;; *.java files that are not included in package
   ....)
```

and you have a file `src/java/testing/Dog.java`

```java
package testing;
public class Dog{
  public int legs = 3;
  public Dog(){};
}
```

You can load it into your library dynamically using `reimport`

```clojure
(reimport 'testing.Dog)
;;=> 'testing.Dog' imported from <project>/target/reload/testing/Dog.class

(.legs (Dog.))
;; => 3
```

You can then change legs in `testing.Dog` from `3` to `4`, save and go back to your repl:

```clojure
(reimport '[testing Dog]) ;; supports multiple classes
;;=> 'testing.Dog' imported from <project>/target/reload/testing/Dog.class

(.legs (Dog.))
;; => 4
```

If you have more files, ie. copy your Dog.java file to Cat.java and do a global replace:

```clojure
(reimport) ;; will load all classes into your namespace
;;=> 'testing.Dog' imported from <project>/target/reload/testing/Dog.class
;;   'testing.Cat' imported from <project>/target/reload/testing/Cat.class

(.legs (Cat.))
;; => 4
```

Now the pain associated with mixed clojure/java development is gone!

### graft

There are two uses for this particular function:
   - Being able to explore a class as a namespace (see [iroh/>ns](https://github.com/zcaudate/iroh))
   - Creating short namespaces accessible from everywhere

This example shows the use case of grafting all the functions in `clojure.set` to `set` as well as all the functions in `java.lang.String` to `str`:

```clojure
(graft '[clojure.set set]
       '[java.lang.String str])
;;=> (#'set/union #'set/map-invert #'set/join #'set/select #'set/intersection #'set/superset? 
;;    #'set/index #'set/subset? #'set/rename #'set/rename-keys ;;#'set/project #'set/difference 
;;    #'str/CASE_INSENSITIVE_ORDER #'str/HASHING_SEED #'str/charAt #'str/checkBounds 
;;    #'str/codePointAt #'str/codePointBefore   ......     #'str/value #'str/valueOf)
``` 

Now that all the functions are installed in their respective namespaces, they can be used from everywhere:

```clojure

(set/union #{1} #{1 2 3}) => #{1 2 3}

(str/value "oeuoeu")
;; => #<char[] [C@1a94720d>
```

### inject

I find that when I am debugging, there are additional functionality that is needed which is not included in clojure.core. The most commonly used function is `pprint` and it is much better if the function came with me when I was debugging.

The best place to put all of these functions in in the `clojure.core` namespace
`inject` is used to add additional functionality to namespaces so that the functions are there right when I need them. Inject also works with macros and functions (unlike `intern` which only works with functions):

```clojure
> (inject 'clojure.core '[[clojure.repl dir]])
;; => will intern #'clojure.repl/dir to #'clojure.core/dir

> (clojure.core/dir clojure.core)
;; *
;; *'
;; *1
;; *2
;; *3
;; *agent*
;; *allow-unresolved-vars*
;; *assert*
;;
;; ...
;; ...
```

`inject` can also work with multiple entries:

```clojure
> (inject 'clojure.core '[[clojure.repl doc source]])
;; => will create the var #'clojure.core/doc and #'clojure.core/source
```

`inject` can also take a prefix:

```clojure
> (inject 'clojure.core '>> '[[clojure.repl doc source]])
;; => will create the var #'clojure.core/>>doc and #'clojure.core/>>source
```

`inject` can use vector bindings to directly specify the name

```clojure
> (inject 'clojure.core '>> '[[clojure.repl doc [source source]]])
;; => will create the var #'clojure.core/>>doc and #'clojure.core/source
```

### inject - installation

`inject` allows easy customisation of your clojure.core namespace by allowing injecting of the functions that you have always wanted to have in your `profiles.clj` file. Here is an example taken from my `profiles.clj`.

```clojure
{:user {:plugins [...]
         :dependencies [[spyscope "0.1.4"]
                        [org.clojure/tools.namespace "0.2.4"]
                        [io.aviso/pretty "0.1.8"]
                        [leiningen "2.3.4"]
                        [im.chit/vinyasa "0.2.1"]]
         :injections [(require 'spyscope.core)
                      (require 'vinyasa.inject)
                      (vinyasa.inject/inject 'clojure.core
                        '[[vinyasa.inject inject]
                          [vinyasa.pull pull]
                          [vinyasa.graft graft]
                          [vinyasa.lein lein]
                          [vinyasa.reimport reimport]])
                      (vinyasa.inject/inject 'clojure.core '>
                        '[[cemerick.pomegranate add-classpath get-classpath resources]
                          [clojure.tools.namespace.repl refresh]
                          [clojure.repl apropos dir doc find-doc source pst
                                        [root-cause >cause]]
                          [clojure.pprint pprint]
                          [clojure.java.shell sh]])]}}
```
I have now imported the following vars into clojure.core and they will stay with me as I am coding in emacs:

   - from vinyasa:
     - `inject` as `#'clojure.core/inject`
     - `pull` as `#'clojure.core/pull`
     - `lein` as `#'clojure.core/lein`
     - `graft` as `#'clojure.core/graft`
     - `reimport` as `#'clojure.core/reimport`
   - from tools.namespace:
     - `refresh` as `#'clojure.core/refresh`
   - from clojure.repl:
     - `apropos` as `#'clojure.core/>apropos`
     - `dir` as `#'clojure.core/>dir`
     - `doc` as `#'clojure.core/>doc`
     - `find-doc` as `#'clojure.core/>find-doc`
     - `root-cause` as `#'clojure.core/>cause``
     - `pst` as `#'clojure.core/>pst`
   - from clojure.pprint:
     - `pprint` as `#'clojure.core/>pprint`
   - from clojure.java.shell:
     - `sh` as `#'clojure.core/>sh`
   - from cemerick.pomegranate:
     - `add-classpath` as `#'clojure.core/>add-classpath`
     - `get-classpath` as `#'clojure.core/>get-classpath`
     - `resources` as `#'clojure.core/>resources`


## License

Copyright © 2014 Chris Zheng

Distributed under the MIT License
