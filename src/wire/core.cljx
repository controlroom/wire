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
  (-lay [this criteria context]
    (wire (-> data
              (update-in [:context] merge context)
              (update-in [:criteria] merge criteria))))
  (-tap [this criteria f]
    (wire (update-in data [:taps (set criteria)] conj f)))
  (-act [this criteria payload]
    (let [fs (find-tap-fns (merge (:criteria data) criteria) (:taps data))]
      (if (not (empty? fs))
        (doseq [f fs]
          (f (merge {::wire this}
                    (:context data)
                    payload)))))
    this))

(defn data
  "Get at the wire's data structure. It's a private thing."
  [wire]
  (-data wire))

(defn- keyed-criteria
  "When unmapped value is used as criteria:

  (lay wire :my-key {:data some data})

  We translate the value to ensure that we dont override any other unmapped
  keys:

  {:__key-:my-key :__key-:my-key}

  This keeps the root criteria hashmap generally conflict free. We do the same
  thing for {:key :my-key} as we consider :key to be the specialest key."
  [criteria]
  (assert (not (sequential? criteria)) "Only hashmaps, strings, and keywords as critera")
  (letfn [(keyworded [key] (keyword (str "__key-" key)))]
    (cond
      (and (map? criteria) (contains? criteria :key))
        (let [key (:key criteria)]
          (merge
            (dissoc criteria :key)
            {(keyworded key) (keyworded key)}))
      (map? criteria)
        criteria
      :else
        {(keyworded criteria) (keyworded criteria)})))

(defn lay
  "Allows you to inject both data and critera into your wire. The data can only
  be retrieved by an owner and only when a wire is acted upon. Attaching data
  allows for components to have state, yet behave anonymously from that state.
  The critera is merged with any act criteria down the wire"
  [wire criteria & data]
  (-lay wire (keyed-criteria criteria) (first data)))

(defn tap
  "Attaches a wiretap listener to some criteria. When the wire is acted upon,
  it looks for matching criteria on collected wiretaps"
  [wire criteria f]
  (-tap wire (keyed-criteria criteria) f))

(defn taps
  "Allow attachment of multiple taps at once. Can list keys/fns in pairs.

  (wire/taps wire
    :key-1         (fn [o] (do-something o))
    {:other :keys} (fn [o] (somethind-else o)))"
  [wire & taps]
  (reduce (fn [w [key f]] (tap w key f)) wire (partition 2 taps)))

(defn act
  "Send a message up the wire."
  ([wire criteria]
   (act wire criteria {}))
  ([wire criteria payload]
   (-act wire (keyed-criteria criteria) payload)))

(defn wire
  "Create a new wire"
  ([]
   (Wire. {:id (str (gensym)) :criteria {} :context {} :taps {}}))
  ([data]
   (Wire. data)))
