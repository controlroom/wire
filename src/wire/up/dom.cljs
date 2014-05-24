(ns wire.up.dom
  (:require [wire.up.events :as events]
            [wire.up.core]
            [wire.core]
            [clojure.string :refer [lower-case]]))

(defn event-fn [dom type action]
  (fn [evt]
    (let [wires (aget dom "__wires")
          data {:type type, :action action, :evt evt} ]
      (if (not (empty? wires))
        (let [criteria    (wire.up.core/build-criteria data)
              return-data (wire.up.core/build-data data)]
          (doseq [wire wires]
            (wire.core/act wire criteria return-data)))))))

(defn keyword->event [kw]
  (-> (str kw)
      (subs 1)
      (lower-case)))

(defn inject-events [wire dom]
  (doseq [[k f] (events/build-mouse-events (partial event-fn dom))]
    (aset dom (keyword->event k) #(f %))))

(defn wire-up
  [wire dom]
  (if-let [wires (aget dom "__wires")]
    (aset dom "__wires" (conj wires wire))
    (do (inject-events wire dom)
        (aset dom "__wires" [wire])))
  wire)

(defn unwire  [wire dom])
