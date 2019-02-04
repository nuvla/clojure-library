(ns sixsq.slipstream.client.impl.utils.json-test
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go]]))
  (:require
    [clojure.core.async :refer #?(:clj  [chan <! >! go <!!]
                                  :cljs [chan <! >!])]
    [clojure.test :refer [#?(:cljs async) are deftest is run-tests testing]]
    [sixsq.slipstream.client.impl.utils.error :as e]
    [sixsq.slipstream.client.impl.utils.json :as t]))


(def body-example {:alpha         1
                   :beta          "2"
                   :gamma         3.0
                   :delta         false
                   :kw-ns/kw-name true})


(deftest check-body-as-json-trans
  (let [body body-example
        json (t/edn->json body)
        req {:body json}]
    (is (= body-example (first (eduction (t/body-as-json) [req]))))))


(defn body-tests
  ([]
   (body-tests nil))
  ([done]
   (go
     (let [body body-example
           json (t/edn->json body)
           c (chan 1 (t/body-as-json) identity)
           _ (>! c {:body json})
           result (<! c)]
       (is (= body result)))
     (when done (done)))))


(deftest check-body-tests
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
           c (chan 1 (t/body-as-json) identity)
           _ (>! c ex)
           result (<! c)]
       (is (e/error? result))
       (is (= msg #?(:clj  (.getMessage result)
                     :cljs (.-message result))))
       (is (= data (ex-data result))))
     (when done (done)))))


(deftest check-exception-tests
  #?(:clj  (<!! (exception-tests))
     :cljs (async done (exception-tests done))))


(defn key-coersion
  "Keywords keys (namespaced or not) should roundtrip exactly. Namespaced
   symbols should retain the namespace when converted to keywords. Everything
   else should be a keyword based on the string representation of the key."
  ([]
   (key-coersion nil))
  ([done]
   (go
     (let [sym (symbol "sym-name")
           sym-ns (symbol "sym-namespace" "sym-name")
           roundtrip (-> (assoc body-example sym-ns true
                                             sym true)
                         t/edn->json
                         t/json->edn)
           expected (assoc body-example (keyword sym-ns) true
                                        (keyword sym) true)]
       (is (= expected roundtrip)))
     (when done (done)))))


(deftest check-keyword-roundtrip-tests
  #?(:clj  (<!! (key-coersion))
     :cljs (async done (key-coersion done))))
