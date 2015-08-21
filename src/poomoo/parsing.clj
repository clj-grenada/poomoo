(ns poomoo.parsing
  "Functions for parsing documtenation files and code examples."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [instaparse.core :as insta]
            [net.cgrand.enlive-html :as enlive]
            [plumbing.core :as plumbing :refer [<- safe-get]]))

;;;; Helpers for parsing

(defn- conveniently-parse
  "Parses S with the grammars read from RS.

  Throws an exception on parse errors. Output is in Enlive format."
  [s & rs]
  (let [parse-res
        (->> rs
             (map io/resource)
             (map slurp)
             (string/join "\n") ; https://github.com/Engelberg/instaparse#string-to-combinator-conversion
             (<- (insta/parser :output-format :enlive)
                 (insta/parse s)))]
    (when (insta/failure? parse-res)
      (throw (IllegalArgumentException.
               (str "Couldn't parse string " s ": " (pr-str parse-res)))))
    parse-res))

(defn- get-unit
  "Runs enlive/select on PARSE-RES and returns the content of the first node
  that matches [KW]."
  [parse-res kw]
  (-> parse-res
      (enlive/select [kw])
      first
      (get :content)))

(defn- safe-get-unit
  "Like get-unit, but throws an exception if there is no node matching [KW]."
  [parse-res kw]
  (if-let [unit (first (enlive/select parse-res [kw]))]
    (safe-get unit :content)
    (throw (IllegalArgumentException.
             (str "Parse result " parse-res " doesn't contain unit: " kw)))))

(defn- lift-enlive-node
  "Enlive nodes are maps with the keys :tag and :content. Returns a vector (to
  be made into a map entry) [tag content]."
  [{:keys [tag content]}]
  [tag content])


;;;; Public API

(defn- parse-ext-doc-string
  "Takes the contents of an external documentation file and turns them into a
  map:

    {:coords  <coordinates of the Thing the documentation is for>
     :calling <(optional) how to call the concrete thing>
     :docs    <(may be empty) the documentation you wrote>"
  [s]
  (let [parse-res (conveniently-parse s "doc_file.bnf")]
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

(defn parse-string-with-example
  "Given a string with code examples, returns a sequence of maps:

    {:expressions <expressions that are evaluated>
     :input       <(optional) string that is read from STDIN>
     :stderr      <(optional) string that is printed on STDERR>
     :stdout      <(optional> string that is printed on STDOUT>
     :result      <(optional) result of evaluating the :expressions>
     :no-eval     <(optional) indicates that the :expressions shouldn't be
                   evaluated>}"
  [s]
  (let [parse-res (conveniently-parse s "code_examples.bnf")
        chunks (enlive/select parse-res [:chunk])]
    (->> (enlive/at parse-res
                    [:chunk #{:expressions :input :stderr :stdout :result :no-eval}]
                    (fn [node] (update node :content #(string/join "\n" %))))
         (map (fn [achunk]
                (into {} (map lift-enlive-node
                              (safe-get achunk :content))))))))
