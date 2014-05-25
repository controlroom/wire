(ns wire.up.dom
  (:require [wire.up.events :as events]
            [wire.up.core]
            [wire.core]
            [clojure.string :refer [lower-case]]))

(defn event-fn [dom type action]
  (fn [event]
    (let [wires (aget dom "__wires")
          data {:type type, :action action, :event event, :context :dom}]
      (if (not (empty? wires))
        (let [criteria    (wire.up.core/build-criteria data)
              return-data (wire.up.core/build-data data)]
          (doseq [wire wires]
            (wire.core/act wire criteria return-data)))))))

(defn keyword->event [kw]
  (get events/react-handler->dom-listener kw))

(defn inject-events [wire dom]
  (doseq [[kw f] (events/build-mouse-events (partial event-fn dom))]
    (.addEventListener dom (keyword->event kw) #(f %))))

(defn wire-up
  "Attach wire to dom object and inject act fn calles to all appropriate
  events. If already wired up, inject wire into wires property"
  [wire dom]
  (if-let [wires (aget dom "__wires")]
    (aset dom "__wires" (conj wires wire))
    (do (inject-events wire dom)
        (aset dom "__wires" [wire])))
  wire)

(defn unwire
  "Remove wire from dom object"
  [wire dom]
  (let [wires (into [] (remove #(= (:id (wire.core/data wire))
                                   (:id (wire.core/data %)))
                               (aget dom "__wires")))]
    (aset dom "__wires" wires)))
