(ns wire.up.play
  (:require [play.dom]
            [wire.core :as wire]
            [wire.up.play :as play]))

(defn wired-tag [tag]
  `(defn ~tag [wire# & vs#]
     (assert (satisfies? wire/BaseWire wire#)
             "The first argument for a wired tag should be a wire")
     (let [[opts# body#] (play/parse-tag-options vs#)
           opts# (merge (play/inject-acts-for-tag ~(name tag) wire#)
                        {:wire wire#}
                        opts#)]
       (~(symbol (str "play.dom/" (name tag))) opts# body#))))

(defmacro build-tags []
  `(do ~@(clojure.core/map wired-tag play.dom/tags)))
