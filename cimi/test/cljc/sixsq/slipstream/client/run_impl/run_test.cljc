(ns sixsq.slipstream.client.run-impl.run-test
  (:refer-clojure :exclude [get])
  (:require
   [clojure.test :refer [are deftest is run-tests testing]]
   [sixsq.slipstream.client.run-impl.crud :as h]
   [sixsq.slipstream.client.run-impl.run :as t]))

(deftest test-to-ids
  (are [x y] (= x (t/extract-ids y))
             ["1" "2" "3"] "node.1,node.2,node.3"
             ["1" "2" "3"] "node.1 ,  node.2, node.3"
             [] "node2"
             ["123"] "node.123"
             ["2"] "node.1.2"
             ["3"] "node, node.3"))

(deftest test-build-param-url
  (is (= "run/123/foo.1:bar" (t/to-param-uri "123" "foo" 1 "bar"))))

(deftest test-into-params
  (is (= nil (h/merge-request)))
  (is (= nil (h/merge-request nil)))
  (is (= {:b 2} (h/merge-request {:b 2})))
  (is (= {:h {:a 1 :b 2}} (h/merge-request {:h {:a 1}} {:h {:b 2}}))))
