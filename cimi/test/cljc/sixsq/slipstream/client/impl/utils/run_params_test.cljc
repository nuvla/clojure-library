(ns sixsq.slipstream.client.impl.utils.run-params-test
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go]]))
  (:require
    [clojure.core.async :refer #?(:clj  [chan <! >! go <!!]
                                  :cljs [chan <! >!])]
    [clojure.test :refer [#?(:cljs async) are deftest is run-tests testing]]
    [sixsq.slipstream.client.impl.utils.error :as e]
    [sixsq.slipstream.client.impl.utils.json :as json]
    [sixsq.slipstream.client.impl.utils.run-params :as t]))

(def response-example {:headers {:location "OK!"}
                       :status  200})


(deftest check-node-parameter
  (are [expected k] (= expected (t/node-parameter k "v"))
                    nil "bad"
                    nil "bad:"
                    nil ":bad"
                    {"parameter--node--n--p" "v"} "n:p"))


(deftest check-process-kv
  (are [expected k] (= expected (t/process-kv k "v"))
                    nil ""
                    nil " "
                    nil "\t"
                    {t/param-scalable "v"} "scalable"
                    {t/param-tags "v"} "tags"
                    {t/param-keep-running "v"} "keep-running"
                    {t/param-type "v"} "type"
                    {"parameter--node--n--p" "v"} "n:p"
                    {"parameter--other" "v"} "other"))


(deftest check-parse-params
  (is (= {t/param-scalable        "true"
          t/param-tags            "a,b,c"
          t/param-keep-running    "never"
          t/param-type            "Run"
          "parameter--node--n--p" "v"
          "parameter--other"      "v"}
         (t/parse-params {""             "v"
                          " "            "v"
                          "\t"           "v"
                          "scalable"     true
                          "tags"         "a,b,c"
                          "keep-running" :never
                          "type"         "Run"
                          "n:p"          "v"
                          "other"        "v"}))))

(deftest check-assoc-module-uri
  (let [uri "some/module/uri"]
    (is (= {t/param-refqname (str "module/" uri)} (t/assoc-module-uri {} uri)))))


(deftest check-extract-location-trans
  (is (= "OK!" (first (eduction (t/extract-location) [response-example])))))


(defn body-tests
  ([]
   (body-tests nil))
  ([done]
   (go
     (let [c (chan 1 (t/extract-location) identity)
           _ (>! c response-example)
           result (<! c)]
       (is (= "OK!" result)))
     (if done (done)))))


(deftest check-extract-location
  #?(:clj  (<!! (body-tests))
     :cljs (async done (body-tests done))))


(defn exception-tests
  ([]
   (exception-tests nil))
  ([done]
   (go
     (let [msg "msg-to-match"
           data {:dummy "data"}
           ex (ex-info msg data)
           c (chan 1 (t/extract-location) identity)
           _ (>! c ex)
           result (<! c)]
       (is (e/error? result))
       (is (= msg #?(:clj  (.getMessage result)
                     :cljs (.-message result))))
       (is (= data (ex-data result))))
     (if done (done)))))

(deftest check-extract-location-error
  #?(:clj  (<!! (exception-tests))
     :cljs (async done (exception-tests done))))
