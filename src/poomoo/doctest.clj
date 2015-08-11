(ns poomoo.doctest
  "Very primitive rendition of Python's doctest in Clojure."
  (:require [poomoo.parsing :as parsing]))

(defn wrap-in-do [s]
  (str "(do " s ")"))

;; TODO: Evaluate in a temporary namespace. (RM 2015-08-11)
(defn check-examples
  "Reads a string potentially containing code examples and checks whether they
  do what they're supposed to.

  Syntax inspired by the Grimoire conventions (http://conj.io/contributing).

  See also parsing/parse-string-with-example.

  If a map only contains :expressions, they are evaluated. If it also contains a
  :result, the result of the evaluation is compared with it. If the map contains
  anything else, it is ignored.

  Otherwise returns a sequence of those maps where the result of the expressions
  doesn't match the :result with an :actual entry containing the actual result
  assoced. Implies that it returns an empty sequence if all matched.

  Evaluation happens in this namespace. This is a bit silly for various reasons,
  but mikera.cljutils/namespace doesn't take bindings from outside and I don't
  want to take the time for writing something more useful now.

  (Sorry for the brain-dump quality of this doc string.)"
  [s]
  (let [examples (parsing/parse-string-with-example s)
        raw-results
        (for [e examples]
          (case (set (keys e))
            #{:expressions}
            (eval (read-string (:expressions e)))

            #{:expressions :result}
            (let [expected-result (read-string (:result e))
                  actual-result (try
                                  (-> e
                                      :expressions
                                      wrap-in-do
                                      read-string
                                      eval)
                                  (catch Exception exc
                                    exc))]
              (if (= expected-result actual-result)
                e
                (assoc e :actual actual-result)))

            nil))]
    (->> raw-results
         (remove nil?)
         (filter #(contains? % :actual)))))
