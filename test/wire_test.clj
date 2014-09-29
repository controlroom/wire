(ns wire-test
  (:require [clojure.test :refer :all]
            [wire.core :refer :all]))

(deftest unit-find-tap-fns
  (testing "single match"
    (let [r {(wire-set {:b 1}) '(2)}]
      (is (= (find-tap-fns {:b 1} r) '(2)) "can find r")
      (is (empty? (find-tap-fns {:b 4} r)) "will ignore misses")))
  (testing "multiple match"
    (let [r {(wire-set {:b 2 :a :a}) '(12)
                      (set {:c 2}) nil}]
      (is (= (find-tap-fns {:a :a :b 2} r) '(12)))))
  (testing "vector values"
    (let [r {(wire-set {:car :toyota}) '(9)}]
      (is (= (find-tap-fns {:car [:ford :toyota]} r) '(9))))))

(deftest wire-lay
  (testing "updating context"
    (let [laid-wire (lay (wire) :l1 {:foo "info"})]
      (testing "can lay new wire context"
        (is (= (:context (data laid-wire)) {:foo "info"})))
      (testing "can add to context"
        (let [level-2-wire (lay laid-wire :l2 {:bar 12})]
          (is (= (:context (data level-2-wire)) {:foo "info" :bar 12}))))
      (testing "can override previous context data"
        (let [level-2-wire (lay laid-wire :l2 {:foo 9})]
          (is (= (:context (data level-2-wire)) {:foo 9}))))))
  (testing "updating lay criteria"
    (testing "understands basic criteria"
      (let [r (atom nil)]
        (-> (wire)
            (tap :awesome (fn [o] (reset! r o)))
            (lay :awesome)
            (act {:toast :neat} {:test "basic"}))
        (is (= (:test @r) "basic"))))
    (testing "understands complex criteria"
      (let [r (atom nil)]
        (-> (wire)
            (tap {:base :stuff} (fn [o] (reset! r o)))
            (lay {:base :stuff})
            (act :k {:test "something"}))
        (is (= (:test @r) "something")))))
  (testing "laying nils"
    (let [r (atom nil)]
      (-> (wire)
          (tap :killer (fn [o] (reset! r o)))
          (lay nil {:test 12})
          (act :killer {:boop :bloop}))
      (is (= (:test @r) 12))))
  (testing "mutiple lays"
    (let [r (atom nil)]
      (-> (wire)
          (tap :killer (fn [o] (reset! r o)))
          (lay [:radical :killer] {:test 12})
          (lay :another nil)
          (act :nope {:boop :bloop}))
      (is (= (:test @r) 12)))))

(deftest wire-tap-basics
  (testing "tap can listen to basic key messages"
    (testing "- without lay"
      (let [r (atom nil) ]
        (-> (wire)
            (tap :k (fn [o] (reset! r o)))
            (act :k {:test "string"}))
        (is (= (:test @r) "string"))))
    (testing "- with lay"
      (let [r (atom nil)]
        (-> (wire)
            (tap :k (fn [o] (reset! r o)))
            (lay :l1)
            (act :k {:test "awesome"}))
        (is (= (:test @r) "awesome")))))
  (testing "multiple taps can listen to an act"
    (let [r (atom 0)]
      (-> (wire)
          (tap :k (fn [o] (swap! r inc)))
          (tap :k (fn [o] (swap! r + 2)))
          (lay :noop)
          (act :k {}))
      (is (= @r 3))))
  (testing "context from lay is merged into a tap"
    (let [r (atom nil)]
      (-> (wire)
          (tap :k (fn [o] (reset! r o)))
          (lay :l1 {:id 1})
          (act :k {:data nil}))
      (is (= (:id @r) 1)))))

(deftest wire-tap-deep-cuts
  (testing "implicit criteria keys do not get overridden"
    (let [r (atom nil)]
      (-> (wire)
          (tap :awesome (fn [o] (reset! r o)))
          (tap :facebook (fn [o] (reset! r o)))
          (lay :awesome)
          (act :facebook {:test "basic"}))
      (is (= (:test @r) "basic")))
    (let [r (atom nil)]
      (-> (wire)
          (tap :awesome (fn [o] (reset! r o)))
          (lay :awesome)
          (act :neat {:test "basic"}))
      (is (= (:test @r) "basic"))))
  (testing "implicit critiera keys can be accessed with complex criteria"
    (let [r (atom nil)]
      (-> (wire)
          (tap {:key :awesome :other :b} (fn [o] (reset! r o)))
          (lay :awesome)
          (act {:other :b} {:test "basic"}))
      (is (= (:test @r) "basic"))))
  (testing "can act on multiple non-key keys"
    (let [r (atom nil)]
      (-> (wire)
          (tap {:other :c} (fn [o] (reset! r o)))
          (lay :awesome)
          (act {:other [:b :c]} {:test "basic"}))
      (is (= (:test @r) "basic"))))
  (testing "vectors as taps results in multiple key checks"
    (let [r (atom nil)]
      (-> (wire)
          (tap [:neat] (fn [o] (reset! r o)))
          (lay :awesome)
          (act {:key :neat} {:test "basic"}))
      (is (= (:test @r) "basic")))))

(deftest wire-act-basics
  (testing "act payload will override context"
    (let [r (atom nil)]
      (-> (wire)
          (tap :k (fn [o] (reset! r o)))
          (lay :l1 {:data nil})
          (act :k  {:data :something}))
      (is (= (:data @r) :something))))
  (testing "act can be called without payload"
    (let [r (atom nil)]
      (-> (wire)
          (tap :k (fn [o] (reset! r o)))
          (lay :l1 {:data :something})
          (act :k))
      (is (= (:data @r) :something)))))

(deftest wire-tap-multiple-criteria
  (let [match {:data :something}]
   (testing "can tap and act with map criteria"
    (testing "- same criteria"
      (let [r (atom nil)]
        (-> (wire)
            (tap {:type :input} (fn [o] (reset! r o)))
            (act {:type :input} match))
        (is (= (:data @r) :something))))
    (testing " - different criteria"
      (let [r (atom nil)]
        (-> (wire)
            (tap {:other :basic} (fn [o] (reset! r o)))
            (act {:type :something :other :basic} match))
        (is (= (:data @r) :something))))
     (testing " - criteria with a miss"
       (let [r (atom nil)]
         (-> (wire)
             (tap {:type :input :key :once} (fn [o] (reset! r o)))
             (act {:type :input} match))
         (is (not= @r match)))))))

(deftest wire-taps
  (testing "can create multiple wiretaps at once"
    (let [r (atom 0)]
      (-> (wire)
          (taps
            :r (fn [o] (swap! r inc))
            :r (fn [o] (swap! r inc)))
          (act :r))
      (is (= @r 2))))
  (testing "combined taps"
    (let [r (atom 0)]
      (-> (wire)
          (taps
            :r (fn [o] (swap! r inc)))
          (taps
            :f (fn [o] (reset! r :fill)))
          (lay :real-stuff)
          (act :f))
      (is (= @r :fill)))))

(deftest wire-tests
  (unit-find-tap-fns)
  (wire-lay)
  (wire-tap-basics)
  (wire-tap-deep-cuts)
  (wire-act-basics)
  (wire-tap-multiple-criteria)
  (wire-taps))
