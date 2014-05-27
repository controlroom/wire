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

(def form-event-terms
  [[:onChange "change" "change"]
   [:onInput  "input"  "input"]
   [:onSubmit "submit" "submit"]])

(def focus-event-terms
  [[:onFocus  "focus"  "focus"]
   [:onBlur   "blur"   "blur"]])

(def scroll-event-terms
  [[:onScroll "scroll" "scroll"]])

(def wheel-event-terms
  [[:onWheel  "wheel"  "wheel"]])

;; Cached data transformations
(def all-terms
  (concat
    keyboard-event-terms
    mouse-event-terms
    form-event-terms
    focus-event-terms
    scroll-event-terms
    wheel-event-terms))

(def react-handler->dom-listener
  (into {} (map (fn [[react-h dom-l _]] [react-h dom-l]) all-terms)))


(defn delayed-event-fn-wrapper [event-fn]
  (fn [event]
    (js/setTimeout #(event-fn event) 100)))

;; Event builders
(defn build-events
  "Build all event functions.

  We make an exception for onBlur since a delay is used to ensure that the blur
  is registered after all other possible actions. Clicking out of a textbox for
  example"
  [event-fn type terms]
  (into {}
    (map (fn [[react-h _ show-h]]
              (if (= :onBlur react-h)
                [react-h (delayed-event-fn-wrapper (event-fn type show-h))]
                [react-h (event-fn type show-h)]))
         terms)))

(defn build-mouse-events [event-fn]
  (build-events event-fn :mouse mouse-event-terms))

(defn build-keyboard-events [event-fn]
  (build-events event-fn :keyboard keyboard-event-terms))

(defn build-form-events [event-fn]
  (build-events event-fn :form form-event-terms))

(defn build-focus-events [event-fn]
  (build-events event-fn :focus focus-event-terms))

(defn build-scroll-events [event-fn]
  (build-events event-fn :scroll scroll-event-terms))

(def default-event-group
  [build-mouse-events
   build-scroll-events])

(def event-group-1
  [["input" "textarea" "select" "option" "button"]
   [build-keyboard-events build-form-events build-focus-events]])

(def event-group-2
  [["form" "optgroup" "fieldset" "label"]
   [build-form-events build-focus-events]])

(defn events-for-tag
  "inject event builders based on tag type."
  [tag event-fn]
  (apply merge
    (map
      (fn [event-build-fn] (event-build-fn event-fn))
      (-> default-event-group
        (cond-> (some (partial = tag) (first event-group-1))
                (concat (second event-group-1)))
        (cond-> (some (partial = tag) (first event-group-2))
                (concat (second event-group-2)))))))
