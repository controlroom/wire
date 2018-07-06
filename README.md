**Wire** is a Clojure(Script) library for sanely managing component
communication

Wire is still a _proof of concept_ and is undergoing breaking changes.

## Simple usage

```clojure
(require '[wire.core :as w])

;; Create new wire and wire tap for a message. Any change functions
;; (tap, lay) are immutable and create a new wire.
(def basic-wire 
  (-> (w/wire)
      (w/tap :hello 
        (fn [{:keys [msg user]}] 
          (println (str "hello " msg " from " (or user "nobody")))))))

;; Send message up the wire
(w/act basic-wire :hello {:msg "world"}) ;; => hello world from nobody

;; Create a new wire with extra data laid 
(def richs-wire
  (w/lay basic-wire nil {:user "rich"}))

;; Send message as you would, with extra laid context
(w/act richs-wire :hello {:msg "world"}) ;; => hello world from rich

;; The initial wire is untouched
(w/act basic-wire :hello {:msg "world"}) ;; => hello world from nobody
```

Wire pairs well with [Show](https://github.com/controlroom/show). Check out [wired-show](https://github.com/controlroom/wired-show) to see how you can easily use wire with dom objects.

## License

Copyright © 2018 controlroom.io

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
