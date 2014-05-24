(ns wire.core
  (:require [clojure.set :refer [difference]]))

(defprotocol BaseWire
  (-data [this])
  (-lay [this key context])
  (-tap [this criteria f])
  (-act [this criteria payload]))

(defn find-tap-fns [act-criteria registered-taps]
  (->> registered-taps
       (filter #(empty? (difference (first %) (set act-criteria))))
       (map #(second %))
       (apply concat)))

(declare wire)

(deftype Wire [data]
  BaseWire
  (-data [this] data)
  (-lay [this key context]
    (wire (-> data
              (update-in [:context] merge context)
              (update-in [:key] conj key))))
  (-tap [this criteria f]
    (wire (update-in data [:taps (set criteria)] conj f)))
  (-act [this criteria payload]
    (let [fs (find-tap-fns criteria (:taps data))]
      (if (not (empty? fs))
        (doseq [f fs]
          (f (merge {::wire this}
                    (:context data)
                    payload)))))))

(defn context [wire]
  (:context (-data wire)))

(defn lay
  "Allows you to inject both data and a namespace into your wire. The data and
  namespace can only be retrieved by an owner and only when a wire is acted
  upon. Attaching data allows for components to have state, yet behave
  anonymously from that state, allowing for simple creation and testing"
  [wire key & data]
  (-lay wire key (first data)))

(defn- keyed-criteria [criteria]
  (if (map? criteria) criteria {:key criteria}))

(defn tap
  "Attaches a wiretap listener to some criteria. When the wire is acted upon,
  it looks for matching criteria on collected wiretaps"
  [wire criteria f]
  ;; Map criteria to :key if anything other an map is passed
  (-tap wire (keyed-criteria criteria) f))

(defn act
  "Send a message up the wire."
  ([wire criteria]
   (act wire criteria {}))
  ([wire criteria payload]
   (-act wire (keyed-criteria criteria) payload)))

(defn wire
  "Create a new wire"
  [& data]
  (if (empty? data)
    (Wire. {:key [] :context {} :taps {}})
    (Wire. (first data))))
