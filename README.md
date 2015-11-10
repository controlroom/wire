**Wire** is a Clojure(Script) library for sanely managing component
communication

Wire is still a proof of concept and is undergoing breaking changes.

## Simple usage

Wire pairs well with [Show](https://github.com/controlroom/show)

```clojure
(ns basic-wired
  (:require [show.core    :as show]
            [show.dom     :as dom]
            [wire.up.show :as wired]
            [wire.core    :as w]))

;; Create a wire and tap on a message
(def root-wire
  (w/tap (w/wire)
    :mouse-click (fn [evt] (println "root wire heard mouse-click"))))

;; Since we laid the id data into the wire, there is no need to worry about
;; keeping track of anything other than selection state
(show/defclass Widget [component]
  (render [{:keys [selected wire name]} _]
    (dom/div
      (wired/button wire name)
      (dom/p {:style {:display (if selected "block" "none")}}
             "You Selected me!"))))

;; Pass the wire into children and tap on messages if you want
(show/defclass App [component]
  (initial-state []
    :selected-widget nil
    :widgets (for [i (range 20)] (str "widget-" i))
    :wire (w/tap root-wire :mouse-click
            #(show/assoc! component :selected-widget (:id %))))
  (render [params {:keys [widgets wire selected-widget]}]
    (dom/div
      (map-indexed (fn [idx name]
                     (Widget {:wire (w/lay wire nil {:id idx})
                              :selected (= selected-widget idx)
                              :name name}))
                   widgets))))

(show/render-component
  (App)
  (.getElementById js/document "app"))
```

A click on the widget button changes the selection and also prints the root-wire
message.

## License

Copyright © 2014 controlroom.io

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
