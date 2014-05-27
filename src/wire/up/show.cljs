(ns wire.up.show
  (:refer-clojure :exclude [map meta time])
  (:require-macros [wire.up.show])
  (:require [show.dom]
            [wire.core]
            [wire.up.core]
            [wire.up.events :as events]))

(defn event-fn [wire type action]
  (fn [event]
    (let [data {:type type, :action action, :event event, :context :show}]
      (wire.core/act wire (wire.up.core/build-criteria data)
                          (wire.up.core/build-data data)))))

(defn inject-acts-for-tag [tag-name wire]
  (events/events-for-tag tag-name (partial event-fn wire)))

(defn parse-tag-options [vs]
  (let [vs (remove nil? vs)]
    (if (map? (first vs))
      [(first vs) (rest vs)]
      [{}          vs])))

(wire.up.show/build-tags)
