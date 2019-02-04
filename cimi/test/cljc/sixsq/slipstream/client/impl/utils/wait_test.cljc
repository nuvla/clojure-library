(ns sixsq.slipstream.client.impl.utils.wait-test
  (:require
    [clojure.test :refer [are deftest is run-tests testing]]
    [sixsq.slipstream.client.impl.utils.wait :as w]))

(deftest test-interval
  (is (= 1 (w/iterations 0 0)))
  (is (= 1 (w/iterations 1 1)))
  (is (= 2 (w/iterations 10 5))))

#?(:clj
   (deftest test-wait-for
     (is (true? (w/wait-for (constantly true) 0 0)))
     (is (true? (w/wait-for (constantly true) 1 1)))
     (is (nil? (w/wait-for (constantly false) 1 1)))))
