(ns wire.up.play
  (:refer-clojure :exclude [map meta time])
  (:require-macros [wire.up.play])
  (:require [play.dom]
            [wire.core :as wire]
            [wire.up.core :as core]
            [wire.up.events :as events]))

(defn event-fn [wire type action]
  (fn [event]
    (let [data {:type type, :action action, :event event}]
      (wire/act wire (core/build-criteria data) (core/build-data data)))))

(defn inject-acts-for-tag [tag-name wire]
  (merge
    (events/build-mouse-events    (partial event-fn wire))
    (events/build-keyboard-events (partial event-fn wire))))

(defn parse-tag-options [vs]
  (let [vs (remove nil? vs)]
    (if (map? (first vs))
      [(first vs) (rest vs)]
      [{}          vs])))

(wire.up.play/build-tags)
