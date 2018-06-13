(ns wire.up.show
  (:refer-clojure :exclude [map meta time])
  (:require-macros [wire.up.show])
  (:require [show.dom]
            [wire.core]
            [wire.up.core]
            [wire.up.events :as events]))

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

(wire.up.show/build-tags)
