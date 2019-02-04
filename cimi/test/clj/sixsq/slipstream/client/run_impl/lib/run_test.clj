(ns sixsq.slipstream.client.run-impl.lib.run-test
  (:require [clojure.test :refer :all]
            [sixsq.slipstream.client.api.deprecated-authn :as a]
            [sixsq.slipstream.client.run-impl.lib.run :as t]))

(def run-uuid "123")
(a/set-context! {:username "user" :password "pass"})

(deftest test-get-comp-ids
  (with-redefs-fn {#'sixsq.slipstream.client.run-impl.lib.run/get-param (fn [_ _ _ _] "")}
    #(is (= '() (t/get-comp-ids run-uuid "foo"))))

  (with-redefs-fn {#'sixsq.slipstream.client.run-impl.lib.run/get-param (fn [_ _ _ _] "1,2,3")}
    #(is (= '("1" "2" "3") (t/get-comp-ids run-uuid "foo"))))

  (with-redefs-fn {#'sixsq.slipstream.client.run-impl.lib.run/get-param (fn [_ _ _ _] "30,10,21")}
    #(is (= '("10" "21" "30") (t/get-comp-ids run-uuid "foo")))))

(deftest test-scale-up
  (with-redefs-fn {#'sixsq.slipstream.client.impl.utils.http-sync/post (fn [_ _] {:body "comp.1,comp.2"})}
    #(is (= ["comp.1" "comp.2"] (t/scale-up run-uuid comp 2))))

  (with-redefs-fn {#'sixsq.slipstream.client.impl.utils.http-sync/post (fn [_ _] {:body "comp.1,comp.2"})
                   #'sixsq.slipstream.client.impl.utils.http-sync/put  (fn [_ _] nil)}
    #(is (= ["comp.1" "comp.2"] (t/scale-up run-uuid comp 2 {"foo" 1 "bar" 2})))))
