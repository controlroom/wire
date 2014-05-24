(ns wire
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
          (f (merge (:context data) payload)))))))

(defn context [wire]
  (:context (-data wire)))

(defn lay
  [wire key & data]
  (-lay wire key (first data)))

(defn keyed-criteria [criteria]
  (if (map? criteria) criteria {:key criteria}))

(defn tap
  [wire criteria f]
  ;; Map criteria to :key if anything other an map is passed
  (-tap wire (keyed-criteria criteria) f))

(defn act
  ([wire criteria]
   (act wire criteria {}))
  ([wire criteria payload]
   (-act wire (keyed-criteria criteria) payload)))

(defn wire
  "Create a new wire, with an optional data map atom"
  [& data]
  (if (empty? data)
    (Wire. {:key [] :context {} :taps {}})
    (Wire. (first data))))
