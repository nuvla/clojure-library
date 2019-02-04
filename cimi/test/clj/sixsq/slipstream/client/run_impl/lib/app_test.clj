(ns sixsq.slipstream.client.run-impl.lib.app-test
  (:require [clojure.test :refer :all]
            [sixsq.slipstream.client.run-impl.lib.app :refer :all]))

(deftest test-app-uri-to-req-params
  (is (= {param-refqname "module/"} (app-uri-to-req-params "" {})))
  (is (= {param-refqname "module/foo/bar"} (app-uri-to-req-params "foo/bar" {}))))

(deftest test-parse-params-basic
  (is (= {} (parse-params {})))
  (is (= {} (parse-params {"" ""})))
  (is (= {} (parse-params {"" "foo"})))
  (is (= {} (parse-params {" " "foo"}))))

(deftest test-parse-deployment-params
  (is (= {"mutable" "true"} (parse-params {:scalable true})))
  (is (= {"mutable" "true"} (parse-params {"scalable" true})))
  (is (= {"mutable" "false"} (parse-params {:scalable false})))
  (is (= {"mutable" "false"} (parse-params {"scalable" "false"})))
  (is (= {"type" "Run"} (parse-params {:type "Run"})))
  (is (= {"type" "Machine"} (parse-params {"type" "Machine"})))
  (is (= {"tags" "foo,bar"} (parse-params {:tags "foo,bar"})))
  (is (= {"tags" "foo,bar"} (parse-params {"tags" "foo,bar"})))
  (is (= {"keep-running" "always"} (parse-params {:keep-running :always})))
  (is (= {"keep-running" "always"} (parse-params {"keep-running" "always"})))
  (let [m (parse-params {:keep-running :never :tags "foo,bar"})]
    (is (= "never" (m "keep-running")))
    (is (= "foo,bar" (m "tags")))))

(deftest test-parse-comp-params
  (is (= {"parameter--node--foo--bar" "baz"} (parse-params {"foo:bar" "baz"})))
  (is (= {"parameter--node--foo--bar" "baz"} (parse-params {:foo:bar "baz"})))
  (is (= {"parameter--foo" "bar"} (parse-params {"foo" "bar"})))
  (is (= {"parameter--foo" "bar"} (parse-params {:foo "bar"}))))

(deftest test-parse-params
  (let [m (parse-params {:keep-running :never
                         :tags         "foo,bar"
                         "webapp:p1"   "val1"
                         "db:p2"       "val2"})]
    (is (= "never" (m "keep-running")))
    (is (= "foo,bar" (m "tags")))
    (is (= "val1" (m "parameter--node--webapp--p1")))
    (is (= "val2" (m "parameter--node--db--p2")))))
