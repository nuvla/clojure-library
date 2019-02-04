(ns sixsq.slipstream.client.impl.utils.common-test
  (:require
    [clojure.test :refer [are deftest is run-tests testing]]
    [sixsq.slipstream.client.impl.utils.common :as t]))

(deftest test-ensure-url
  (let [baseUrl "https://nuv.la"
        fullUrl "https://nuv.la/api/resource/id"]
    (is (= (t/ensure-url baseUrl fullUrl) fullUrl))
    (is (= (t/ensure-url baseUrl "/api/resource/id") fullUrl))
    (is (= (t/ensure-url-slash baseUrl fullUrl) fullUrl))
    (is (= (t/ensure-url-slash baseUrl "api/resource/id") fullUrl))))
