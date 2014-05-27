(ns wire.up.events
  (:require [wire.up.core :as core]))

;; Event terms
;; Vector of vectors containing:
;;
;; [React Handler, DOM listener, Wire Action]
(def mouse-event-terms
  [[:onClick       "click"      "click"]
   [:onDoubleClick "dblclick"   "double-click"]
   [:onDrag        "drag"       "drag"]
   [:onDragEnd     "dragend"    "drag-end"]
   [:onDragEnter   "dragenter"  "drag-enter"]
   [:onDragExit    "dragexit"   "drag-exit"]
   [:onDragLeave   "dragleave"  "drag-leave"]
   [:onDragOver    "dragover"   "drag-over"]
   [:onDragStart   "dragstart"  "drag-start"]
   [:onDrop        "drop"       "drop"]
   [:onMouseDown   "mousedown"  "down"]
   [:onMouseEnter  "mouseenter" "enter"]
   [:onMouseLeave  "mouseleave" "leave"]
   [:onMouseMove   "mousemove"  "move"]
   [:onMouseOut    "mouseout"   "out"]
   [:onMouseOver   "mouseover"  "over"]
   [:onMouseUp     "mouseup"    "up"]])

(def keyboard-event-terms
  [[:onKeyDown  "keydown"  "down"]
   [:onKeyUp    "keyup"    "up"]
   [:onKeyPress "keypress" "press"]])

;; Cached data transformations
(def all-terms
  (concat
    keyboard-event-terms
    mouse-event-terms))

(def react-handler->dom-listener
  (into {} (map (fn [[react-h dom-l _]] [react-h dom-l]) all-terms)))

;; Event builders
(defn build-events [event-fn type terms]
  (into {}
    (map (fn [[react-h _ show-h]]
              [react-h (event-fn type show-h)])
         terms)))

(defn build-mouse-events [event-fn]
  (build-events event-fn :mouse mouse-event-terms))

(defn build-keyboard-events [event-fn]
  (build-events event-fn :keyboard keyboard-event-terms))

