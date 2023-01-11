(ns sixsq.nuvla.client.impl.utils.http-utils-test
  (:require
    [clojure.test :refer [deftest is]]
    [sixsq.nuvla.client.impl.utils.http-utils :as h]))

(deftest test-process-req
  (let [req (h/set-or-clear-insecure-flag {})]
    (is (contains? req :kvlt.platform/insecure?))
    (is (not (:kvlt.platform/insecure? req))))
  (let [req (h/set-or-clear-insecure-flag {:a 1})]
    (is (contains? req :a))
    (is (contains? req :kvlt.platform/insecure?))
    (is (not (:kvlt.platform/insecure? req))))
  (let [req (h/set-or-clear-insecure-flag {:insecure? true})]
    (is (not (contains? req :insecure?)))
    (is (contains? req :kvlt.platform/insecure?))
    (is (true? (:kvlt.platform/insecure? req))))
  (let [req (h/set-or-clear-insecure-flag {:insecure? false})]
    (is (not (contains? req :insecure?)))
    (is (contains? req :kvlt.platform/insecure?))
    (is (false? (:kvlt.platform/insecure? req)))))

