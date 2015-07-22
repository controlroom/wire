(ns wire.core
  "Wire is a simple abstraction allowing for message streaming"
  (:require [clojure.set :refer [difference]]))

(defprotocol BaseWire
  (-data [this])
  (-lay [this key context])
  (-tap [this criteria f])
  (-act [this criteria payload]))

(defn wire-set
  "Allow for vectors of values to be used as criteria, we just transpose
  vector keys into another set of parings ex:
  {:a [:b :c]} => #{[:a :b] [:a :c]}"
  [m]
  (set (mapcat #(if (sequential? (second %))
             (map vector (repeat (first %)) (second %))
             [%])
          (into [] m))))

(defn find-tap-fns
  "Search through all registered wiretap fns and use set logic to find any
  criteria matches to execute"
  [act-criteria registered-taps]
  (->> registered-taps
       (filter #(empty? (difference (first %) (wire-set act-criteria))))
       (map #(second %))
       (apply concat)))

(defn group-merge
  "Merge conflicts into vectors. ex:
  (group-merge {:a :b} {:a :c}) => {:a [:b :c]}
  (group-merge {:a [:n :b]} {:a :c}) => {:a [:n :b :c]}"
  [& maps]
  (reduce
    (fn [o h]
      (reduce
        (fn [o [k v]]
          (let [cv (get o k)
                cv (when cv (if (sequential? cv) cv [cv]))
                seq-v       (if (sequential? v)  v  [v])]
            (if cv
              (assoc o k (into cv seq-v))
              (assoc o k v))))
        o h))
    {}
    maps))

(comment
  (apply group-merge
    [{:a [:b :n]} {:a :c :b 3} {{:cool 2} 100} {{:cool 2} [:a 4]}]))

(declare wire)

(deftype Wire [data]
  BaseWire
  (-data [this] data)
  (-lay [this criteria context]
    (wire (-> data
              (update-in [:context] merge context)
              (cond->
                criteria (update-in [:criteria] group-merge criteria)))))
  (-tap [this criteria f]
    (wire (update-in data [:taps (wire-set criteria)] conj f)))
  (-act [this criteria payload]
    (let [criteria (group-merge (:criteria data) criteria)
          fs (find-tap-fns criteria (:taps data))]
      (doseq [f fs]
        (f (merge {::wire    this
                   :criteria criteria}
                  (:context  data)
                  payload))))
    this))

(defn data
  "Get at the wire's data structure. It's a private thing."
  [wire]
  (-data wire))

(defn- keyed-criteria
  "This keeps the root criteria hashmap generally conflict free. We do the same
  thing for {:key :my-key} as we consider :key to be the specialest key."
  [criteria]
  (cond
    (or (nil? criteria) (map? criteria))
      criteria
    :else
      {:key criteria}))

(defn lay
  "Allows you to inject both data and critera into your wire. The data can only
  be retrieved by an owner and only when a wire is acted upon. Attaching data
  allows for components to have state, yet behave anonymously from that state.
  The critera is merged with any act criteria down the wire"
  [wire criteria & data]
  (-lay wire (keyed-criteria criteria) (first data)))

(defn tap
  "Attaches a wiretap listener to some criteria. When the wire is acted upon,
  it looks for matching criteria on collected wiretaps."
  [wire criteria f]
  (-tap wire (keyed-criteria criteria) f))

(defn taps
  "Allow attachment of multiple taps at once. Can list keys/fns in pairs.

  (wire/taps wire
    :key-1         (fn [o] (do-something o))
    {:other :keys} (fn [o] (somethind-else o)))"
  [wire & taps]
  (reduce (fn [w [key f]] (tap w key f)) wire (partition 2 taps)))

(defn mute-tap
  "Ignore any other taps down the tap chain"
  [wire]
  ())

(defn act
  "Send a payload up the wire with criteria."
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
