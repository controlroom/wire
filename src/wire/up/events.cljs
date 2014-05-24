(ns wire.up.events
  (:require [wire.up.core :as core]))

(defn build-keyboard-events [event-fn]
  {:onKeyDown  (event-fn :keyboard "down")
   :onKeyUp    (event-fn :keyboard "up")
   :onKeyPress (event-fn :keyboard "press")})

(defn build-mouse-events [event-fn]
  {:onClick       (event-fn :mouse "click")
   :onDoubleClick (event-fn :mouse "double-click")
   :onDrag        (event-fn :mouse "drag")
   :onDragEnd     (event-fn :mouse "drag-end")
   :onDragEnter   (event-fn :mouse "drag-enter")
   :onDragExit    (event-fn :mouse "drag-exit")
   :onDragLeave   (event-fn :mouse "drag-leave")
   :onDragOver    (event-fn :mouse "drag-over")
   :onDragStart   (event-fn :mouse "drag-start")
   :onDrop        (event-fn :mouse "drop")
   :onMouseDown   (event-fn :mouse "down")
   :onMouseEnter  (event-fn :mouse "enter")
   :onMouseLeave  (event-fn :mouse "leave")
   :onMouseMove   (event-fn :mouse "move")
   :onMouseOut    (event-fn :mouse "out")
   :onMouseOver   (event-fn :mouse "over")
   :onMouseUp     (event-fn :mouse "up")})

