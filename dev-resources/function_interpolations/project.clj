(defproject io.logicblocks/lein-interpolate.function-interpolations "0.1.0"
  :url "https://test.example.com/custom-interpolations"
  :description "A test project for lein-interpolate with custom interpolations"

  :interpolations {:custom/path    ~(fn [project]
                                     (str (:root project) "/some/custom/path"))
                   :custom/version ~(fn [project]
                                     (str (:version project) "-RC1"))}

  :dependencies [[thing.core :custom/version]]

  :profiles
  {:test {:thing-path :custom/path}})
