(ns sixsq.nuvla.client.impl.utils.common-test
  (:require
    [clojure.test :refer [are deftest is run-tests testing]]
    [sixsq.nuvla.client.impl.utils.common :as t]))

(deftest test-ensure-url
  (let [base-url "https://nuv.la"
        full-url "https://nuv.la/api/resource/id"]
    (is (= (t/ensure-url base-url full-url) full-url))
    (is (= (t/ensure-url base-url "/api/resource/id") full-url))
    (is (= (t/ensure-url-slash base-url full-url) full-url))
    (is (= (t/ensure-url-slash base-url "api/resource/id") full-url))))
