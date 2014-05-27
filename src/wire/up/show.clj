(ns wire.up.show
  (:require [show.dom]
            [wire.core :as wire]
            [wire.up.show :as show]))

(defn wired-tag [tag]
  `(defn ~tag [wire# & vs#]
     (assert (satisfies? wire/BaseWire wire#)
             "The first argument for a wired tag should be a wire")
     (let [[opts# body#] (show/parse-tag-options vs#)
           opts# (merge (show/inject-acts-for-tag ~(name tag) wire#)
                        {:wire wire#}
                        opts#)]
       (~(symbol (str "show.dom/" (name tag))) opts# body#))))

(defmacro build-tags []
  `(do ~@(clojure.core/map wired-tag show.dom/tags)))
