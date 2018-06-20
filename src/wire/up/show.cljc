(ns wire.up.show
  #?(:cljs
     (:refer-clojure :exclude [map meta time])
     (:require
       [show.dom]
       [wire.core]
       [wire.up.core]
       [wire.up.events :as events])))

(defn event-fn [wire tag-name opts type action]
  (fn [event]
    (let [data {:type     type
                :action   action
                :event    event
                :tag-name tag-name
                :opts     opts
                :context  :show}]
      (wire.core/act wire (wire.up.core/build-criteria data)
                          (wire.up.core/build-data data)))))

(defn inject-acts-for-tag [tag-name opts wire]
  (events/events-for-tag tag-name (partial event-fn wire tag-name opts)))

(defn array-map? [o]
  (instance? cljs.core/PersistentArrayMap o))

(defn parse-tag-options [vs]
  (let [vs (remove nil? vs)
        end (if (map? (first vs))
              [(first vs) (next vs)]
              [{}         (seq vs)])]
    end))

#?(:clj
   (defn wired-tag [tag]
     `(defn ~tag [wire# & vs#]
        (assert (satisfies? wire.core/BaseWire wire#)
                "The first argument for a wired tag should be a wire")
        (let [[opts# body#] (wire.up.show/parse-tag-options vs#)
              opts# (merge (wire.up.show/inject-acts-for-tag ~(name tag) opts# wire#)
                           {:wire wire#}
                           opts#)]
          (~(symbol (str "show.dom/" (name tag))) opts# body#))))
   (defmacro build-tags []
     `(do ~@(cljs.core/map wired-tag show.dom/tags))))

(build-tags)
