(ns sixsq.slipstream.client.impl.utils.java-test
  (:require [clojure.test :refer [are deftest is]]
            [clojure.walk :as walk]
            [sixsq.slipstream.client.impl.utils.java :as t]))

(deftest check-roundtrip
  (are [input] (= input (-> input t/to-java t/to-clojure))
               {}
               #{}
               []
               '()
               {:alpha "a", :beta true, :gamma 3, :delta nil, :epsilon 42.0}
               {:alpha {:one true, :two 2.0}
                :beta #{"a" 2 3.0 false}
                :gamma [1 2 3 4 "five" "six" "seven"]
                :delta '("something")
                :epsilon 10.2}))

(defn throw-on-keyword
  [x]
  (when (keyword? x)
    (throw (ex-info (str "found keyword: " x) {}))))

(deftest check-no-keywords
  (is (nil? (walk/postwalk throw-on-keyword (t/to-java {:alpha {:one 1, :two 2}, :beta true})))))
