(ns sixsq.slipstream.client.impl.utils.cimi-test
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go]]))
  (:require
    [clojure.core.async :refer #?(:clj  [chan <! >! go <!!]
                                  :cljs [chan <! >!])]
    [clojure.test :refer [#?(:cljs async) are deftest is run-tests testing]]
    [sixsq.slipstream.client.impl.utils.cimi :as t]
    [sixsq.slipstream.client.impl.utils.error :as e]
    [sixsq.slipstream.client.impl.utils.json :as json]))

(def test-cep {:id               "cloud-entry-point"
               :resourceURI      "http://schemas.dmtf.org/cimi/2/CloudEntryPoint"
               :created          "2015-09-01T20:36:16.891Z"
               :updated          "2015-09-01T20:36:16.891Z"
               :baseURI          "https://localhost:8201/api/"
               :attributes       {:href "attribute"}
               :connectors       {:href "connector"}
               :events           {:href "event"}
               :licenses         {:href "license"}
               :licenseTemplates {:href "license-template"}
               :usages           {:href "usage"}
               :networkServices  {:href "network-service"}
               :serviceOffers    {:href "service-offer"}
               :usageRecords     {:href "usage-record"}
               :acl              {:owner {:principal "ADMIN", :type "ROLE"}
                                  :rules [{:principal "ANON", :type "ROLE", :right "VIEW"}]}})

(def ops-example {:operations [{:rel  "add"
                                :href "add"}
                               {:rel  "edit"
                                :href "edit"}
                               {:rel  "delete"
                                :href "delete"}]})

(def other-map {:alpha true
                :beta  true})

(def cimi-map {:$first   1
               :$last    2
               :$select  "alpha"
               :$filter  "a=2"
               :$expand  "beta"
               :$orderby "alpha:asc"})

(deftest cimi-params-handling
  (let [m (merge other-map cimi-map)]
    (is (nil? (t/remove-cimi-params "BAD")))
    (is (nil? (t/remove-cimi-params nil)))
    (is (= {} (t/remove-cimi-params {})))
    (is (= other-map (t/remove-cimi-params m)))

    (is (nil? (t/select-cimi-params "BAD")))
    (is (nil? (t/select-cimi-params nil)))
    (is (= {} (t/select-cimi-params {})))
    (is (= cimi-map (t/select-cimi-params m)))))

(deftest check-state-updates
  (is (nil? (t/update-state (atom nil) nil "OK")))
  (are [expected state input] (= expected (t/update-state (atom state) :token input))
                              nil nil nil
                              nil {} nil
                              {:token "OK"} {} "OK"
                              {:token "OK"} {:token "BAD"} "OK"))

(deftest check-unauthorized
  (let [v (t/unauthorized)
        data (ex-data (first v))]
    (is (vector? v))
    (is (nil? (second v)))
    (is (= 403 (:status data)))
    (is (= "unauthorized" (:message data)))
    (is (= nil (:resource-id data))))
  (let [id "resource/uuid"
        v (t/unauthorized id)
        data (ex-data (first v))]
    (is (= id (:resource-id data)))))

(deftest check-response-tuple
  (let [cookie-value "com.sixsq.slipstream.cookie=ok"
        response {:headers
                          {:server                    "nginx"
                           :content-type              "application/json"
                           :content-length            "156"
                           :strict-transport-security "max-age=31536000; includeSubdomains"
                           :connection                "keep-alive"
                           :location                  "session/ac3251c5-7cc0-4578-9c34-feaf5b91c200"
                           :set-cookie                cookie-value
                           :date                      "Thu, 11 May 2017 08:53:15 GMT"}
                  :status 201
                  :body   "{\"status\" : 201,
                            \"message\" : \"created session/ac3251c5-7cc0-4578-9c34-feaf5b91c200\",
                            \"resource-id\" : \"session/ac3251c5-7cc0-4578-9c34-feaf5b91c200\"}"}
        [resp token] (t/response-tuple response)]
    (is (= cookie-value token))
    (is (= 201 (:status resp)))
    (is (= "session/ac3251c5-7cc0-4578-9c34-feaf5b91c200" (:resource-id resp)))))

(deftest correct-collection-urls
  (is (nil? (t/get-collection-url nil nil)))
  (is (nil? (t/get-collection-url nil "connectors")))
  (are [x y] (= x (t/get-collection-url test-cep y))
             nil nil
             nil "unknownResources"
             nil :unknownResources
             "https://localhost:8201/api/attribute" "attributes"
             "https://localhost:8201/api/attribute" :attributes
             "https://localhost:8201/api/connector" "connectors"
             "https://localhost:8201/api/connector" :connectors
             "https://localhost:8201/api/event" "events"
             "https://localhost:8201/api/event" :events
             "https://localhost:8201/api/license" "licenses"
             "https://localhost:8201/api/license" :licenses
             "https://localhost:8201/api/license-template" "licenseTemplates"
             "https://localhost:8201/api/license-template" :licenseTemplates
             "https://localhost:8201/api/usage" "usages"
             "https://localhost:8201/api/usage" :usages
             "https://localhost:8201/api/network-service" "networkServices"
             "https://localhost:8201/api/network-service" :networkServices
             "https://localhost:8201/api/service-offer" "serviceOffers"
             "https://localhost:8201/api/service-offer" :serviceOffers
             "https://localhost:8201/api/usage-record" "usageRecords"
             "https://localhost:8201/api/usage-record" :usageRecords))

(deftest check-verify-collection-url
  (is (nil? (t/verify-collection-url nil nil)))
  (is (nil? (t/verify-collection-url (dissoc test-cep :baseURI) nil)))
  (are [url] (= url (t/verify-collection-url test-cep url))
             "https://localhost:8201/api/attribute"
             "https://localhost:8201/api/connector"
             "https://localhost:8201/api/event"
             "https://localhost:8201/api/license"
             "https://localhost:8201/api/license-template"
             "https://localhost:8201/api/usage"
             "https://localhost:8201/api/network-service"
             "https://localhost:8201/api/service-offer"
             "https://localhost:8201/api/usage-record"))

(deftest check-legacy-endpoint
  (let [cep-url "https://nuv.la/api/cloud-entry-point"]
    (is (= "https://nuv.la/module" (t/legacy-endpoint cep-url "module")))
    (is (= "https://nuv.la/run" (t/legacy-endpoint cep-url "run")))))

(deftest check-extract-op-url-tests
  (let [baseURI "https://localhost:8201/api/"
        body ops-example
        json (json/edn->json body)
        req {:body json}]
    (are [op] (= (str baseURI op) (first (eduction (t/extract-op-url op baseURI) [req])))
              "add"
              "edit"
              "delete")))

(defn extract-op-url-tests
  ([]
   (extract-op-url-tests nil))
  ([done]
   (go
     (let [baseURI "https://localhost:8201/api/"
           body ops-example
           json (json/edn->json body)]
       (let [c (chan 1 (t/extract-op-url "add" baseURI) identity)
             _ (>! c {:body json})
             result (<! c)]
         (is (= (str baseURI "add") result)))
       (let [c (chan 1 (t/extract-op-url "edit" baseURI) identity)
             _ (>! c {:body json})
             result (<! c)]
         (is (= (str baseURI (name "edit")) result)))
       (let [c (chan 1 (t/extract-op-url "delete" baseURI) identity)
             _ (>! c {:body json})
             result (<! c)]
         (is (= (str baseURI "delete") result))))
     (if done (done)))))

(deftest check-extract-op-url-tests-with-chan
  #?(:clj  (<!! (extract-op-url-tests))
     :cljs (async done (extract-op-url-tests done))))
