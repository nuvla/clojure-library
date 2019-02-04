(ns sixsq.slipstream.client.impl.utils.error-test
  (:require
    [clojure.test :refer [are deftest is]]
    [sixsq.slipstream.client.impl.utils.error :as t]))

(deftest test-error-predicate
  (are [x y] (= x (t/error? y))
             true #?(:clj  (Exception. "dummy msg")
                     :cljs (js/Error "dummy msg"))
             true (ex-info "dummy msg" {})
             false 1
             false "1"
             false 1.0
             false true))

(deftest test-not-thrown
  (are [x] (= x (t/throw-if-error x))
           1
           "1"
           1.0
           true
           {}
           []))

(deftest test-thrown
  (is (thrown? #?(:clj Exception :cljs js/Error)
               (t/throw-if-error (ex-info "dummy-msg" {})))))
