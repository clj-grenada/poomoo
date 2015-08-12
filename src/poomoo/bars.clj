(ns poomoo.bars
  "Defines a few Bar types useful for documenting concrete things."
  {:grenada.cmeta/bars {:poomoo.bars/markup-all :common-mark}}
  (:require [grenada
             [bars :as b]
             [things :as t]]
            [grenada.things.def :as things.def]
            [plumbing.core :refer [safe-get safe-get-in]]
            [schema.core :as s]))

(def docs-def
  "Definition of the Bar type `::docs`.

  ## Model

  This Bar allows attaching **additional documentation** to a Thing. It is
  thought for **augmenting or substituting** documentation attached with
  `:grenada.bars/doc` and for attaching documentation to Things that are not
  Finds or Namespaces, but Platforms, Versions, Artifacts or Groups.

  Furthermore, this Bar allows attaching **multiple pieces** of documentation to
  a Thing. Each consists of a string key that uniquely identifies it, and the
  documentation string itself.

  ## Prerequisites

  None. Can be attached to any Thing.

  ## Remarks

  The **key** should be fairly unique, but can be the same for all Things you're
  annotating. It is thought to allow the merging of `::docs` maps:

  ```
  #g/t [:grenada.things/thing
        {:coords [… \"map\"
         :aspects …
         :bars
         {…
          :poomoo.bars/docs
          {\"eberhards-docs\"
           \"You could think of map as the morphism morphism of the sequence
            functor.\"}}]}]

  and

  #g/t [:grenada.things/thing
        {:coords [… \"map\"
         :aspects …
         :bars
         {…
          :poomoo.bars/docs
          {\"doros-docs\"
           \"map is a funny function. It lets you reverse every word in a list
            of words. …\"}}]}]

  combine to:

  #g/t [:grenada.things/thing
        {:coords [… \"map\"
         :aspects …
         :bars
         {…
          :poomoo.bars/docs
          {\"eberhards-docs\"
           \"You could think of map as the morphism morphism of the sequence
            functor.\"

           \"doros-docs\"
           \"map is a funny function. It lets you reverse every word in a list
            of words. …\"}}]}]
  ```

  (If Eberhard the Formal's characterization of `map` is wrong, please tell me
  and I will forward your comments to him.)

  `::docs` can also be used to put **other project documentation** in a Datadoc
  JAR, for example the README and the files from the `doc` directory. In this
  case I recommend using a path relative to the project root as the key."
  (things.def/map->bar-type
    {:name ::docs
     :schema {s/Str s/Str}}))

(defn markup-lang-valid? [bar]
  (contains? #{:common-mark :github-mark :html :plain} bar))

(def docs-markup-def
  "Definition of the Bar type `::docs-markup`.

  ## Model

  `::docs-markup` Bars specify the markup language used for the `::docs` Bar of
  a Thing. Other Bars and **consumers dealing** with doc strings should take
  this into account.

  Currently the following markup **languages** are **allowed**:

   - [CommonMark](http://commonmark.org/)
   - [GitHub-flavoured
     Markdown](https://help.github.com/articles/github-flavored-markdown/)
   - [HTML](http://www.w3.org/TR/html/)
   - [plain text](http://www.unicode.org/versions/Unicode6.1.0/ch02.pdf)

  This Bar type does not **guarantee** that the documentation processor at the
  other end will be able to handle any of these formats properly.

  ## Prerequisites

  Can be attached to Things with arbitrary Aspects. Can only be attached to
  Things that already have a `::docs` Bar.

  ## Remarks

  Do not use Bars of this type for specifying the markup language of **doc
  strings** and `:grenada.bars/doc` Bars. That's what `::markup` is for.

  If you want to specify the markup language for a **whole bunch of Things** at
  once, you can use `:docs-markup-all`."
  (things.def/map->bar-type
    {:name ::docs-markup
     :bar-prereqs-pred #(contains? % ::docs)
     :valid-pred markup-lang-valid?}))

(def docs-markup-all-def
  "Definition of the Bar type `::docs-markup-all`.

  ## Model

  Attaching a Bar of type `:poomoo.bars/docs-markup-all` to a Thing T has the
  same meaning as attaching a Bar of type `:poomoo.bars/docs-markup-all` to T
  and those of its descendants that don't have a `:poomoo.bars/docs-markup` or
  `:poomoo.bars/docs-markup-all` Bar already. For details, see the definition of
  `:poomoo.bars/docs-markup`.

  Note: this means that if you're building a tool that has to do with
  `:poomoo.bars` Bars, you also have to **look at the ancestors** of Things
  whether they say something about the markup.

  ## Prerequisites

  Can only be attached to Things above and including the Namespace level.

  ## Remarks

  See the remarks on the definition of `:poomoo.bars/docs-markup`."
  (things.def/map->bar-type
    {:name ::docs-markup-all
     :bar-prereqs-pred #(t/above-incl? ::t/namespace (t/pick-main-aspect %))
     :valid-pred markup-lang-valid?}))

(def markup-def
  "Definition of the Bar type `::markup`.

  ## Model

  This Bar is the same as `::docs-markup` except that it specifies the markup
  language used for **Clojure doc strings** and `:grenada.bars/doc` Bars instead
  of `:poomoo.bars/docs` Bars. See the definition of `::docs-markup` for
  details.

  ## Prerequisites

  Can only be attached to Finds and Namespace, which have to already have a
  `::grenada.bars/doc` Bar.

  ## Remarks

  If you want to specify the markup language for many Things at once, use
  `::markup-all`."
  (things.def/map->bar-type
    {:name ::markup
     :aspect-prereqs-pred (safe-get-in b/def-for-bar-type
                                       [::b/doc :aspect-prereqs-pred])
     :bar-prereqs-pred #(contains? % ::b/doc)
     :valid-pred markup-lang-valid?}))

(def markup-all-def
  "Definition of the Bar type `::markup-all`.

  ## Model

  This Bar is to `::markup` the same as `::docs-markup-all` is to `::markup`.
  See their definitions for more information.

  ## Prerequisites

  Can only be attached to Things above and including the Namespace level."
  (things.def/map->bar-type
    {:name ::markup-all
     :aspect-prereqs-pred #(t/above-incl? ::t/namespace (t/pick-main-aspect %))
     :valid-pred markup-lang-valid?}))

(def def-for-bar-type
  (things.def/map-from-defs #{docs-def
                              docs-markup-def
                              docs-markup-all-def
                              markup-def
                              markup-all-def}))
