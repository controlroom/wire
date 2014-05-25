(ns wire.up.core
  (:require [wire.utils :as utils]
            [wire.core  :as wire]))

(defn keycode->descriptor [code]
  (get utils/keycode-map code))

;; Build specific criteria
(defmulti  build-criteria :type)

(defmethod build-criteria :keyboard [data]
  (let [base {:type   :dom
              :class  :keyboard
              :key    (keyword (str "keyboard-" (:action data)))
              :action (keyword (:action data))}
        desc (keycode->descriptor (.-keyCode (:event data)))]
    (-> base
        (cond-> desc (assoc :keypress desc)))))

(defmethod build-criteria :mouse [data]
  {:type    :dom
   :class   :mouse
   :key    (keyword (str "mouse-" (:action data)))
   :action (keyword (:action data))})


;; Build specific data
(defmulti  build-data :type)

(defmethod build-data :keyboard [data]
  {:event (:event data)})

(defmethod build-data :mouse [data]
  {:event (:event data)})


