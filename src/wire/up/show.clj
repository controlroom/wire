(ns wire.up.show)

(defn wired-tag [tag]
 `(defn ~tag [wire# & vs#]
    (assert (satisfies? wire.core/BaseWire wire#)
            "The first argument for a wired tag should be a wire")
    (let [[opts# body#] (wire.up.show/parse-tag-options vs#)
          opts# (merge (wire.up.show/inject-acts-for-tag ~(name tag) opts# wire#)
                       opts#)]
      (apply show.dom/element ~(str tag) opts# body#))))

(defmacro build-tags []
 `(do ~@(map wired-tag show.dom/tags)))
