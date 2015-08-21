(defproject org.clj-grenada/poomoo "1.0.0-rc.3"
  :description "A collection of Bars and tools for Clojure Grenada Things"
  :url "https://github.com/clj-grenada/poomoo"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [enlive "1.1.6"]
                 [instaparse "1.4.1"]
                 [org.clj-grenada/lib-grenada "1.0.0-rc.4"]
                 [prismatic/plumbing "0.4.3"]
                 [prismatic/schema "0.4.0"]]

  :codox {:sources ["src"]
          :output-dir "api-docs"
          :src-dir-uri "https://github.com/clj-grenada/poomoo/blob/master/"
          :homepage-uri "https://github.com/clj-grenada/poomoo/tree/master/"
          :src-linenum-anchor-prefix "L"})
