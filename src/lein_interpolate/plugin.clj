(ns lein-interpolate.plugin
  (:require
    [clojure.pprint :as pprint]
    [clojure.walk :as walk]))

(defn interpolate-tokens [project]
  (let [version (:version project)]
    (walk/postwalk
      (fn [node]
        (if (= node :project/version)
          version
          node))
      project)))

(defn middleware [project]
  (pprint/pprint project)
  (interpolate-tokens project))
