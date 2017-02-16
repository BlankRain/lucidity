(ns documentation.lucid-git
  (:require [lucid.git :as git]))

[[:chapter {:title "Introduction"}]]

"`lucid.git` is used to as an interface to [jgit](https://eclipse.org/jgit/).

The aims of this project are:

- self-directed exploration of the jgit library 
- simple and easy to use interface
"
[[:section {:title "Installation"}]]

"Add to `project.clj` dependencies:

    [im.chit/lucid.git \"{{PROJECT.version}}\"]

All functionality is in the `lucid.git` namespace:"

(comment
  (require '[lucid.git :refer [git]]))

[[:chapter {:title "Usage"}]]

[[:section {:title "Help"}]]

"To see all commands, there is the `:help` command:"

(comment
  
  (git :help) ;; or (git)
  => [:add :apply :archive :blame :branch :checkout :cherry
      :clean :clone :commit :describe :diff :fetch :gc :init
      :log :ls :merge :name :notes :pull :push :rebase :reflog
      :remote :reset :revert :rm :stash :status :submodule :tag])

"To see a command's sub-commands, just "

(comment
  
  (git :branch)
  => #{:create :delete :rename :list})

[[:section {:title "Basics"}]]

[[:subsection {:title ":init"}]]

"The `:init` command initialise a directory:"

(comment
  (git :init :directory "/tmp/git-example")
  => "/tmp/git-example/.git")

[[:subsection {:title ":status"}]]

"The `:status` command checks the status of our new repository:"

(comment

  (git "/tmp/git-example" :status)
  => {:clean? true
      :uncommitted-changes! false})

"Having a directory as the first parameter sets the default directory so that next time `:status` is called, it doesn't need to be set:"

(comment
  
  (git :status)
  => {:clean? true
      :uncommitted-changes! false})

"We can add a file and check for the repository status:"

(comment
  
  (spit "/tmp/git-example/hello.txt" "hello there")
  (git :status)
  => {:clean? false,
      :uncommitted-changes! false,
      :untracked #{"hello.txt"}})

[[:subsection {:title ":cd"}]]

"`:cd` sets the default directory:"

(comment
  
  (git :cd "/tmp/git-example")
  (git :status)
  => {:clean? false,
      :uncommitted-changes! false,
      :untracked #{"hello.txt"}})

[[:subsection {:title ":pwd"}]]

"`:pwd` retruns the current working directory:"

(comment
  (git :pwd)
  => "/tmp/git-example")

[[:section {:title "Exploration"}]]

"Help is avaliable at anytime by using `:?` or `:help` after the first command:"

(comment
  
  (git :init :?)
  => {:bare boolean,
      :directory java.lang.String,
      :git-dir java.lang.String})

"Additional parameters may be put in:"

(comment
  
  (git :init
       :bare false
       :directory "/tmp/git-example"
       :git-dir "/tmp/git-example/.mercurial")
  => "/tmp/git-example/.mercurial")


"Let's take a closer look at `:status`"

(comment
  
  (git :status :?)
  => {:working-tree-it   org.eclipse.jgit.treewalk.WorkingTreeIterator, 
      :progress-monitor  org.eclipse.jgit.lib.ProgressMonitor, 
      :ignore-submodules #{"NONE" "UNTRACKED" "DIRTY" "ALL"}, 
      :path              [java.lang.String]})

"We can decode the representation of the options needed for:

- `:working-tree-it` an input of type `org.eclipse.jgit.treewalk.WorkingTreeIterator`
- `:progress-monitor` an input of type `org.eclipse.jgit.lib.ProgressMonitor`
- `:ignore-submodules` an input of the following options `#{\"NONE\" \"UNTRACKED\" \"DIRTY\" \"ALL\"}` 
- `:path`, either a single or a vector input of type `java.lang.String`"

[[:section {:title "Local"}]]

[[:subsection {:title ":add"}]]

"Options that `:add` take are:"

(comment
  
  (git :add :?)
  => {:filepattern [java.lang.String]
      :update boolean
      :working-tree-iterator org.eclipse.jgit.treewalk.WorkingTreeIterator})

"We can now create three files and calling `:add` on `hello.txt`"

(comment
  (git :init :directory "/tmp/git-example")
  (git :cd "/tmp/git-example")
  (do (spit "/tmp/git-example/hello.txt" "hello")
      (spit "/tmp/git-example/world.txt" "world")
      (spit "/tmp/git-example/again.txt" "again"))

  (git :add :filepattern ["hello.txt"])
  => {"hello.txt" #{:merged}})

[[:subsection {:title ":commit"}]]

"Options that `:commit` take are:"

(comment

  (git :commit :?)
  => {:all boolean
      :allow-empty boolean
      :amend boolean
      :author java.lang.String
      :committer java.lang.String
      :hook-output-stream java.lang.String
      :insert-change-id boolean
      :message java.lang.String
      :no-verify boolean
      :only java.lang.String
      :reflog-comment java.lang.String})

"The revision can now be committed:"

(comment

  (git :commit :message "added hello.txt")
  => {:commit-time 1487219645,
      :author-ident {:email-address "z@caudate.me",
                     :name "Chris Zheng",
                     :time-zone-offset 660,
                     :when #inst "2017-02-16T04:34:05.000-00:00"},
      :full-message "added hello.txt",
      :name "c115771e38cfd22954cfbc0c1a5c0b7e7890b09f"})

"`:commit` works like the shell `git commit` command, such as if the message is neede to be amended:"

(comment
  
  (git :commit :message "added hello.txt with fix" :amend true)
  => {:commit-time 1487219780,
      :author-ident {:email-address "z@caudate.me",
                     :name "Chris Zheng",
                     :time-zone-offset 660,
                     :when #inst "2017-02-16T04:34:05.000-00:00"},
      :full-message "added hello.txt with fix",
      :name "fa705232c13d19d3ef4b4b6cfd6993593615a55d"})

[[:subsection {:title ":log"}]]

"``"

(comment
  (git :log)
  => [{:commit-time 1487219780,
       :author-ident {:email-address "z@caudate.me",
                     :name "Chris Zheng",
                     :time-zone-offset 660,
                     :when #inst "2017-02-16T04:34:05.000-00:00"},
       :full-message "added hello.txt with fix",
       :name "fa705232c13d19d3ef4b4b6cfd6993593615a55d"}]
  )

[[:subsection {:title ":rm"}]]

"`:rm` removes files from git:"

(comment
  (git :rm :?)
  => {:filepattern [java.lang.String], :cached boolean})

"`hello.txt` is removed and the revision committed"

(comment
  (git :rm :filepattern ["hello.txt"])
  (git :add :filepattern ["again.txt"])
  => {"again.txt" #{:merged}}

  (git :commit :message "added again.txt, removed hello.txt")
  => {:commit-time 1487219780,
       :author-ident {:email-address "z@caudate.me",
                      :name "Chris Zheng",
                      :time-zone-offset 660,
                      :when #inst "2017-02-16T04:34:05.000-00:00"},
       :full-message "added hello.txt with fix",
      :name "fa705232c13d19d3ef4b4b6cfd6993593615a55d"}
  )

[[:subsection {:title "workflow"}]]

"`:log` and `:status` show more information about the revision whilst more changes can be committed"

(comment

  (git :log)
  => [{:commit-time 1487223636,
       :author-ident {:email-address "z@caudate.me",
                      :name "Chris Zheng",
                      :time-zone-offset 660,
                      :when #inst "2017-02-16T05:40:36.000-00:00"},
       :full-message "added again.txt, removed hello.txt",
       :name "f00dae4b70f00daf90816ece100d49678b0f9271"}
      {:commit-time 1487219780,
       :author-ident {:email-address "z@caudate.me",
                      :name "Chris Zheng",
                      :time-zone-offset 660,
                      :when #inst "2017-02-16T04:34:05.000-00:00"},
       :full-message "added hello.txt with fix",
       :name "fa705232c13d19d3ef4b4b6cfd6993593615a55d"}]

  (git :status)
  => {:untracked ["world.txt"] :clean? false}

  (git :add :filepattern ["."])
  => {"again.txt" #{:merged}, "world.txt" #{:merged}}

  (git :commit :message "added ALL files" :amend true)
  => {:commit-time 1487224441,
      :author-ident {:email-address "z@caudate.me"
                     :name "Chris Zheng",
                     :time-zone-offset 660,
                     :when #inst "2017-02-16T05:52:34.000-00:00"},
      :full-message "added ALL files",
      :name "02cecb2b8c08c1599ba95c165e08b633698cad99"}

  (git :status)
  => {:clean? true})

[[:section {:title "Raw Objects"}]]

"When `:&` is used in the parameter, the raw result of the commant call is returned instead of being converted into a corresponding map/string:"

(comment

  (git :status :&)
  ;; => #status{:conflicting []
  ;;            :untracked-folders []
  ;;            :missing []
  ;;            :removed []
  ;;            :clean? true}

  (type (git :status :&))
  => org.eclipse.jgit.api.Status

  (type (git :log :&))
  => org.eclipse.jgit.revwalk.RevWalk)

[[:section {:title "Branching"}]]

[[:subsection {:title ":branch"}]]

"The subcommands for :branch are:"

(comment

  (git :branch)
  => #{:create :delete :rename :list})

"Branches for the repository are shown with the `:list` subcommand:"

(comment

  (git :branch :list)
  => [{:name "refs/heads/master",
       :object-id "02cecb2b8c08c1599ba95c165e08b633698cad99",
       :storage "LOOSE",
       :peeled? false,
       :symbolic? false}])

"New branches are be created through the `:create` subcommand:"

(comment

  (git :branch :create :name "dev")
  => {:name "refs/heads/dev",
      :object-id "02cecb2b8c08c1599ba95c165e08b633698cad99",
      :storage "LOOSE",
      :peeled? false,
      :symbolic? false}
  
  (git :branch :list)
  => [{:name "refs/heads/dev",
       :object-id "02cecb2b8c08c1599ba95c165e08b633698cad99",
       :storage "LOOSE",
       :peeled? false,
       :symbolic? false}
      {:name "refs/heads/master",
       :object-id "02cecb2b8c08c1599ba95c165e08b633698cad99",
       :storage "LOOSE",
       :peeled? false,
       :symbolic? false}])

"Branches are renamed through the `:rename` subcommand:"

(comment

  (git :branch :rename :?)
  => {:new-name java.lang.String,
      :old-name java.lang.String}

  (git :branch :rename :new-name "development" :old-name "dev")
  => {:name "refs/heads/development",
      :object-id "02cecb2b8c08c1599ba95c165e08b633698cad99",
      :storage "LOOSE",
      :peeled? false,
      :symbolic? false})

"Branches are deleted through the `:delete` subcommand:"

(comment

  (git :branch :delete :?)
  => {:branch-names [java.lang.String]
      :force boolean}

  (git :branch :delete :branch-names (into-array ["development"]))
  => ["refs/heads/development"])


[[:subsection {:title ":checkout"}]]

"Branches for the repository can be accessed through `:checkout`"

(comment

  (git :checkout :?)
  => {:path [java.lang.String],
      :start-point java.lang.String,
      :create-branch boolean,
      :stage #{"BASE" "THEIRS" "OURS"},
      :force boolean,
      :name java.lang.String,
      :paths [java.util.List],
      :upstream-mode #{"SET_UPSTREAM" "NOTRACK" "TRACK"},
      :all-paths boolean,
      :orphan boolean}

  (git :branch :create :name "dev")

  (git :checkout :name "dev")
  => {:name "refs/heads/dev",
      :object-id "02cecb2b8c08c1599ba95c165e08b633698cad99",
      :storage "LOOSE",
      :peeled? false,
      :symbolic? false}
)
