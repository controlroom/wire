(defproject controlroom/wire "0.2.0"
  :jar-exclusions  [#"\.cljx|\.swp|\.swo|\.DS_Store"]
  :description "Sane intercomponent communication"
  :url "http://controlroom.io/wire"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]]
  :cljsbuild {:builds []})
