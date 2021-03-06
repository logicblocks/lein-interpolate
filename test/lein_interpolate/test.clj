(ns lein-interpolate.test
  (:require
   [clojure.test :refer :all]
   [clojure.string :as string]

   [leiningen.core.project :as project]

   [lein-interpolate.plugin :as plugin]))

(defn snake-case [value]
  (string/replace value "-" "_"))

(defn project-path [project-name]
  (str "./dev-resources/" (snake-case (name project-name)) "/project.clj"))

(def dependency-id first)
(def dependency-version second)

(defn find-dependency [dependencies id]
  (first
    (filter
      (fn [dependency]
        (= id (dependency-id dependency)))
      dependencies)))

(defn read-project [project-name]
  (-> project-name
    project-path
    project/read
    plugin/middleware))

(deftest interpolates-project-attributes-when-referenced
  (let [project (read-project :project-attributes)
        version (:version project)
        root (:root project)
        group (:group project)
        description (:description project)]

    (let [dependencies (get project :dependencies)
          dependency (find-dependency dependencies 'thing.core/thing.core)]
      (is (= version (dependency-version dependency))))

    (let [profiles (get project :profiles)
          profile (get profiles :test)]
      (is (= root (:thing-path profile))))

    (let [aliases (get project :aliases)
          alias (get aliases :tweet)]
      (is (= ["tweet" ":group" group ":description" description] alias)))))

(deftest interpolates-custom-interpolations-when-referenced
  (let [project (read-project :custom-interpolations)
        interpolations (:interpolations project)
        version (:custom/version interpolations)
        path (:custom/path interpolations)
        arg (:custom/arg interpolations)]

    (let [dependencies (get project :dependencies)
          dependency (find-dependency dependencies 'thing.core/thing.core)]
      (is (= version (dependency-version dependency))))

    (let [profiles (get project :profiles)
          profile (get profiles :test)]
      (is (= path (:thing-path profile))))

    (let [aliases (get project :aliases)
          alias (get aliases :tweet)]
      (is (= ["tweet" ":arg" arg] alias)))))

(deftest allows-interpolation-values-to-be-functions-of-project
  (let [project (read-project :function-interpolations)
        interpolations (:interpolations project)
        version (:custom/version interpolations)
        path (:custom/path interpolations)]

    (let [dependencies (get project :dependencies)
          dependency (find-dependency dependencies 'thing.core/thing.core)]
      (is (= (version project) (dependency-version dependency))))

    (let [profiles (get project :profiles)
          profile (get profiles :test)]
      (is (= (path project) (:thing-path profile))))))
