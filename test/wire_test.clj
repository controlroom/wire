(ns wire-test
  (:require [clojure.test :refer :all]
            [wire :refer :all]))

(deftest unit-find-tap-fns
  (testing "single match"
    (let [registered {(set {:b 1}) '(2)}]
      (testing "can find registered"
        (is (= (find-tap-fns {:b 1} registered) '(2))))
      (testing "will ignore misses"
        (is (empty? (find-tap-fns {:b 4} registered)))))))

(deftest wire-lay
  (let [laid-wire (lay (wire) :l1 {:foo "info"})]
    (testing "can lay new wire context"
      (is (= (context laid-wire) {:foo "info"})))
    (testing "can add to context"
      (let [level-2-wire (lay laid-wire :l2 {:bar 12})]
        (is (= (context level-2-wire) {:foo "info" :bar 12}))))
    (testing "can override previous context data"
      (let [level-2-wire (lay laid-wire :l2 {:foo 9})]
        (is (= (context level-2-wire) {:foo 9}))))))

(deftest wire-tap-basics
  (testing "tap can listen to basic key messages"
    (testing "- without lay"
      (let [r (atom nil)]
        (-> (wire)
            (tap :k (fn [o] (reset! r o)))
            (act :k {}))
        (is (= @r {}))))
    (testing "- with lay"
      (let [r (atom nil)]
        (-> (wire)
            (tap :k (fn [o] (reset! r o)))
            (lay :l1)
            (act :k {}))
        (is (= @r {})))))
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
      (is (= @r {:id 1 :data nil})))))

(deftest wire-act-basics
  (testing "act payload will override context"
    (let [r (atom nil)]
      (-> (wire)
          (tap :k (fn [o] (reset! r o)))
          (lay :l1 {:data nil})
          (act :k  {:data :something}))
      (is (= @r {:data :something}))))
  (testing "act can be called without payload"
    (let [r (atom nil)]
      (-> (wire)
          (tap :k (fn [o] (reset! r o)))
          (lay :l1 {:data :something})
          (act :k))
      (is (= @r {:data :something})))))

(deftest wire-tap-multiple-criteria
  (let [match {:data :something}]
   (testing "can tap and act with map criteria"
    (testing "- same criteria"
      (let [r (atom nil)]
        (-> (wire)
            (tap {:type :input} (fn [o] (reset! r o)))
            (act {:type :input} match))
        (is (= @r match))))
    (testing " - different criteria"
      (let [r (atom nil)]
        (-> (wire)
            (tap {:other :basic} (fn [o] (reset! r o)))
            (act {:type :something :other :basic} match))
        (is (= @r match))))
     (testing " - criteria with a miss"
       (let [r (atom nil)]
         (-> (wire)
             (tap {:type :input :key :once} (fn [o] (reset! r o)))
             (act {:type :input} match))
         (is (not= @r match)))))))

(deftest wire-tests
  (unit-find-tap-fns)
  (wire-lay)
  (wire-tap-basics)
  (wire-act-basics)
  (wire-tap-multiple-criteria))
