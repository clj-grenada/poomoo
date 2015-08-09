(ns poomoo.bars
  (:require [grenada.things.def :as things.def]))

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

(def def-for-bar-type
  (things.def/map-from-defs #{docs-def}))
