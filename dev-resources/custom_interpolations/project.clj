(defproject io.logicblocks/lein-interpolate.custom-interpolations "0.1.0"
  :url "https://test.example.com/custom-interpolations"
  :description "A test project for lein-interpolate with custom interpolations"

  :interpolations {:custom/path "some/custom/path"
                   :custom/version "1.2.3"
                   :custom/arg "some-arg"}

  :dependencies [[thing.core :custom/version]]

  :profiles
  {:test {:thing-path :custom/path}}

  :aliases {:tweet
            ["tweet"
             ":arg" :custom/arg]})
