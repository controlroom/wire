(ns wire.up.play
  (:refer-clojure :exclude [map meta time])
  (:require-macros [wire.up.play])
  (:require [play.dom]
            [wire.up.core :as core]))

(defn event-fn [wire type action]
  (fn [evt] (core/build-act type wire action evt)))

(defn build-keyboard-events [wire]
  {:onKeyDown  (event-fn wire :keyboard "down")
   :onKeyUp    (event-fn wire :keyboard "up")
   :onKeyPress (event-fn wire :keyboard "press")})

(defn build-mouse-events [wire]
  {:onClick       (event-fn wire :mouse "click")
   :onDoubleClick (event-fn wire :mouse "double-click")
   :onDrag        (event-fn wire :mouse "drag")
   :onDragEnd     (event-fn wire :mouse "drag-end")
   :onDragEnter   (event-fn wire :mouse "drag-enter")
   :onDragExit    (event-fn wire :mouse "drag-exit")
   :onDragLeave   (event-fn wire :mouse "drag-leave")
   :onDragOver    (event-fn wire :mouse "drag-over")
   :onDragStart   (event-fn wire :mouse "drag-start")
   :onDrop        (event-fn wire :mouse "drop")
   :onMouseDown   (event-fn wire :mouse "down")
   :onMouseEnter  (event-fn wire :mouse "enter")
   :onMouseLeave  (event-fn wire :mouse "leave")
   :onMouseMove   (event-fn wire :mouse "move")
   :onMouseOut    (event-fn wire :mouse "out")
   :onMouseOver   (event-fn wire :mouse "over")
   :onMouseUp     (event-fn wire :mouse "up")})

(defn inject-acts-for-tag [tag-name wire]
  (merge
    (build-mouse-events wire)
    (build-keyboard-events wire)))

(defn parse-tag-options [vs]
  (let [vs (remove nil? vs)]
    (if (map? (first vs))
      [(first vs) (rest vs)]
      [{}          vs])))

(wire.up.play/build-tags)
