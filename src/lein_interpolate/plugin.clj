(ns lein-interpolate.plugin
  (:require
   [clojure.walk :as walk]))

(defn interpolate-tokens [project]
  (let [interpolations (get project :interpolations {})
        interpolatable-project (dissoc project :interpolations)
        interpolated-project
        (walk/postwalk
          (fn [node]
            (if (keyword? node)
              (let [kns (keyword (namespace node))
                    k (keyword (name node))]
                (cond
                  (= kns :project) (k project)
                  (contains? interpolations node) (node interpolations)
                  :else node))
              node))
          interpolatable-project)]
    (merge interpolated-project
      {:interpolations interpolations})))

(defn middleware [project]
  (interpolate-tokens project))
