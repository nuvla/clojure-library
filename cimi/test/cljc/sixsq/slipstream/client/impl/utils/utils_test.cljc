(ns sixsq.slipstream.client.impl.utils.utils-test
  (:require
    [clojure.test :refer [are deftest is run-tests testing]]
    [sixsq.slipstream.client.impl.utils.utils :as t]))

(deftest test-in?
  (is (true? (t/in? :a [:a :b])))
  (is (true? (t/in? :a #{:a :b})))
  (is (true? (t/in? :a '(:a :b))))

  (is (false? (t/in? :a [:b])))
  (is (false? (t/in? :a #{:b})))
  (is (false? (t/in? :a '(:b)))))

(deftest test-url-join
  (is (= "" (t/url-join)))
  (is (= "" (t/url-join [])))
  (is (= "a/b" (t/url-join ["a" "b"])))
  (is (= "http://example.com/r1/id1" (t/url-join ["http://example.com" "r1" "id1"]))))

(deftest test-to-body-params
  (is (= "" (t/to-body-params {})))
  (is (= "" (t/to-body-params {"" ""})))
  (is (= "" (t/to-body-params {" " ""})))
  (is (= "" (t/to-body-params {nil "b"})))
  (is (= "" (t/to-body-params {"" "b"})))
  (is (= "a=b" (t/to-body-params {:a "b"})))
  (is (= "a=b" (t/to-body-params {"a" "b"})))
  (is (= "a=b&c=d" (t/to-body-params {"a" "b" "c" "d"})))
  (is (= "a=b&c=d" (t/to-body-params {:a "b" :c "d"})))
  (is (= "a=b\nc=d" (t/to-body-params {"a" "b" "c" "d"} "\n"))))

