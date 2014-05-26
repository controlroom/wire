(ns wire.up.core
  (:require [wire.up.utils :as utils]
            [wire.core     :as wire]))

(defn keycode->descriptor [code]
  (get utils/keycode-map code))

(defn base-dom-criteria [type action]
  {:type   :dom
   :class  (keyword type)
   :key    (keyword (str type "-" action))
   :action (keyword action)})

;; Build specific criteria
(defmulti  build-criteria :type)

(defmethod build-criteria :keyboard [data]
  (let [base (base-dom-criteria "keyboard" (:action data))
        desc (keycode->descriptor (.-keyCode (:event data)))]
    (-> base
        (cond-> desc (assoc :keypress desc)))))

(defmethod build-criteria :mouse [data]
  (base-dom-criteria "mouse" (:action data)))

;; Build specific data
(defmulti  build-data :type)

(defmethod build-data :keyboard [data]
  {:event (:event data)})

(defmethod build-data :mouse [data]
  {:event (:event data)})


