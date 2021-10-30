(ns lein-interpolate.plugin
  (:require
    [clojure.walk :as walk]))

(defn interpolate-tokens [project]
  (walk/postwalk
    (fn [node]
      (if (keyword? node)
        (let [kns (keyword (namespace node))
              k (keyword (name node))]
          (if (= kns :project)
            (k project)
            node))
        node))
    project))

(defn middleware [project]
  (interpolate-tokens project))
