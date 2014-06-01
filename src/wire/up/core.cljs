(ns wire.up.core
  (:require [wire.up.utils :as utils]
            [wire.core     :as wire]
            [clojure.string :as string]))

(defn keycode->descriptor [code]
  (get utils/keycode-map code))

(defn base-dom-criteria [type data]
  (let [action (:action data)
        opts   (:opts data)]
    (-> {:type   :dom
         :group  (keyword type)
         :key    (keyword (str type "-" action))
         :tag    (keyword (:tag-name data))
         :action (keyword action)}
        (cond-> (contains? opts :id)
          (assoc :id (keyword (:id opts))))
        (cond-> (and (contains? opts :className) (not (empty? (:className opts))))
          (assoc :class
                 (mapv keyword (string/split (:className opts) #" ")))))))

;; Build specific criteria
(defmulti  build-criteria :type)

(defmethod build-criteria :mouse [data]
  (base-dom-criteria "mouse" data))

(defmethod build-criteria :keyboard [data]
  (let [base (base-dom-criteria "keyboard" data)
        desc (keycode->descriptor (.-keyCode (:event data)))]
    (-> base
        (cond-> desc (assoc :keypress desc)))))

(defmethod build-criteria :form [data]
  (base-dom-criteria "form" data))

(defmethod build-criteria :focus [data]
  (base-dom-criteria "focus" data))

(defn base-data [data]
  (let [event (:event data)]
    {:target   (.-target event)
     :value    (if-let [target (.. event -target)]
                 (.-value target)
                 nil)
     :type     (.-type event)
     :event    event}))

;; Build specific data
(defmulti  build-data :type)

(defmethod build-data :mouse [data]
  (base-data data))

(defmethod build-data :keyboard [data]
  (base-data data))

(defmethod build-data :form [data]
  (let [event (:event data)]
    (merge (base-data data)
           {:value (.. event -target -value)})))

(defmethod build-data :focus [data]
  (base-data data))
