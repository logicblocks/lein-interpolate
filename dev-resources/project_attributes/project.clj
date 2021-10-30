(defproject io.logicblocks/lein-interpolate.project-attributes "0.1.0"
  :url "https://test.example.com/project-attributes"
  :description "A test project for lein-interpolate with project attribute interpolations"

  :dependencies [[thing.core :project/version]]

  :profiles
  {:test {:thing-path :project/root}}

  :aliases {:tweet
            ["tweet"
             ":group" :project/group
             ":description" :project/description]})
