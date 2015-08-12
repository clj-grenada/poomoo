(ns poomoo.parsing
  "Functions for parsing documtenation files and code examples."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [instaparse.core :as insta]
            [net.cgrand.enlive-html :as enlive]
            [plumbing.core :as plumbing :refer [<- safe-get]]))

;;;; Helpers for parsing

(defn- parser-from-resources [& rs]
  (->> rs
       (map io/resource)
       (map slurp)
       (string/join "\n") ; https://github.com/Engelberg/instaparse#string-to-combinator-conversion
       (<- (insta/parser :output-format :enlive))))

(defn get-unit [parse-res kw]
  (-> parse-res
      (enlive/select [kw])
      first
      (get :content)))

(defn safe-get-unit [parse-res kw]
  (if-let [unit (first (enlive/select parse-res [kw]))]
    (safe-get unit :content)
    (throw (IllegalArgumentException.
             (str "Parse result " parse-res " doesn't contain unit: " kw)))))


;;;; Public API

(defn parse-ext-doc-string [s]
  (let [parse-res (-> (parser-from-resources "doc_file.bnf")
                      (insta/parse s))]
    (when (insta/failure? parse-res)
      (throw (IllegalArgumentException.
               (str "Couldn't parse string " s ": " (pr-str parse-res)))))
    (plumbing/assoc-when
      {:coords (-> parse-res
                   (safe-get-unit :coordinates)
                   first
                   edn/read-string)
       :contents (->> (safe-get-unit parse-res :contents)
                      (string/join "\n"))}
      :calling (-> parse-res
                   (get-unit :calling)
                   first
                   (as-> x (str "(" x ")"))
                   edn/read-string))))
