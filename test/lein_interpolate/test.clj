(ns lein-interpolate.test
  (:require
   [clojure.test :refer :all]
   [clojure.java.io :as io]

   [leiningen.core.project :as project]

   [lein-interpolate.plugin :as plugin])
  (:import
   [java.io File Writer]))

(defmacro with-temporary-project [binding & body]
  `(let [project# (File/createTempFile "project" ".clj")
         ~(first binding) (.getAbsolutePath project#)]
     (.deleteOnExit project#)
     (with-open [writer# (io/writer project#)]
       (.write ^Writer writer# ^String (str ~(second binding))))
     ~@body))

(def dependency-id first)
(def dependency-version second)

(defn find-dependency [dependencies id]
  (first
    (filter
      (fn [dependency]
        (= id (dependency-id dependency)))
      dependencies)))

(defn read-project [project-path]
  (-> project-path
    project/read
    plugin/middleware))

(deftest interpolates-project-attributes-when-referenced
  (with-temporary-project
    [project-path
     '(defproject project-attributes "0.1.0"
        :url "https://test.example.com/project-attributes"
        :description "Testing basic project attribute interpolation"

        :dependencies [[thing.core :project/version]]

        :profiles
        {:test {:thing-path :project/root}}

        :aliases {:tweet
                  ["tweet"
                   ":group" :project/group
                   ":description" :project/description]})]
    (let [project (read-project project-path)
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
        (is (= ["tweet" ":group" group ":description" description] alias))))))

(deftest interpolates-custom-interpolations-when-referenced
  (with-temporary-project
    [project-path
     '(defproject custom-interpolations "0.1.0"
        :url "https://test.example.com/custom-interpolations"
        :description "Testing custom interpolations"

        :interpolations {:custom/path    "some/custom/path"
                         :custom/version "1.2.3"
                         :custom/arg     "some-arg"}

        :dependencies [[thing.core :custom/version]]

        :profiles
        {:test {:thing-path :custom/path}}

        :aliases {:tweet
                  ["tweet"
                   ":arg" :custom/arg]})]
    (let [project (read-project project-path)
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
        (is (= ["tweet" ":arg" arg] alias))))))

(deftest allows-interpolation-values-to-be-functions-of-project
  (with-temporary-project
    [project-path
     '(defproject function-interpolations "0.1.0"
        :url "https://test.example.com/function-interpolations"
        :description "Testing functions as interpolations"

        :interpolations
        {:custom/path    ~(fn [project]
                            (str (:root project) "/some/custom/path"))
         :custom/version ~(fn [project]
                            (str (:version project) "-RC1"))}

        :dependencies [[thing.core :custom/version]]

        :profiles
        {:test {:thing-path :custom/path}})]
    (let [project (read-project project-path)
          interpolations (:interpolations project)
          version (:custom/version interpolations)
          path (:custom/path interpolations)]

      (let [dependencies (get project :dependencies)
            dependency (find-dependency dependencies 'thing.core/thing.core)]
        (is (= (version project) (dependency-version dependency))))

      (let [profiles (get project :profiles)
            profile (get profiles :test)]
        (is (= (path project) (:thing-path profile)))))))

(deftest resolves-interpolation-dependencies
  (with-temporary-project
    [project-path
     '(defproject interpolation-dependencies "0.1.0"
        :url "https://test.example.com/interpolation-dependencies"
        :description "Testing dependencies between interpolations"

        :interpolations
        {:custom/value "a3b4cd"}

        :dependency-thing :custom/value

        :dependencies [[thing.core :project/dependency-thing]])]
    (let [project (read-project project-path)
          value (get-in project [:interpolations :custom/value])]
      (let [dependencies (get project :dependencies)
            dependency (find-dependency dependencies 'thing.core/thing.core)]
        (is (= value (dependency-version dependency)))))))
