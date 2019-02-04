(ns sixsq.slipstream.client.cimi-async-lifecycle-test
  "Runs lifecycle tests for CIMI resources against a live server. If no user
   credentials are provided, the lifecycle tests are 'no-ops'. To run these
   tests (typically from the REPL), do the following:

   ```clojure
   (require '[sixsq.slipstream.client.cimi-async-lifecycle-test :as t] :reload)
   (in-ns 'sixsq.slipstream.client.cimi-async-lifecycle-test)
   (def ^:dynamic *server-info* (set-server-info \"username\" \"password\" \"https://nuv.la/\"))
   (run-tests)
   ```

   **NOTE**: The value for \"my-server-root\" must end with a slash!
   "
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go]]))
  (:require
    [clojure.core.async :refer #?(:clj  [chan <! >! go <!!]
                                  :cljs [chan <! >!])]
    [clojure.test :refer [#?(:cljs async) are deftest is run-tests testing]]
    [kvlt.core]
    [sixsq.slipstream.client.api.authn :as authn]

    [sixsq.slipstream.client.api.cimi :as cimi]
    [sixsq.slipstream.client.async :as i]))

;; silence the request/response debugging
(kvlt.core/quiet!)

(def example-event
  {:id          "123"
   :resourceURI "http://schemas.dmtf.org/cimi/2/Event"
   :created     "2015-01-16T08:20:00.0Z"
   :updated     "2015-01-16T08:20:00.0Z"

   :timestamp   "2015-01-10T08:20:00.0Z"
   :content     {:resource {:href "Run/45614147-aed1-4a24-889d-6365b0b1f2cd"}
                 :state    "Started"}
   :type        "state"
   :severity    "medium"

   :acl         {:owner {:type      "USER"
                         :principal "loomis"}
                 :rules [{:right     "ALL"
                          :type      "USER"
                          :principal "loomis"}
                         {:right     "ALL"
                          :type      "ROLE"
                          :principal "ADMIN"}]}})

(defn set-server-info [username password server-root]
  (when (and username password server-root)
    (let [endpoint (str server-root "api/cloud-entry-point")]
      {:username     username
       :password     password
       :cep-endpoint endpoint})))

;; FIXME: Caution!  Do not commit credentials.
(def ^:dynamic *server-info* (set-server-info nil nil "https://nuv.la/"))

(defn strip-fields [m]
  (dissoc m :id :created :updated :acl :operations))

;;
;; CAUTION: If too many 'is' tests are added, the clojurescript compiler
;; may cause the stack to overflow.  This is apparently related to the issue
;; http://dev.clojure.org/jira/browse/ASYNC-40
;; The immediate solution is to eliminate some of the less useful tests.
;;

(defn event-lifecycle
  ([]
   (event-lifecycle nil))
  ([done]
   (go
     (if *server-info*
       (let [{:keys [username password cep-endpoint]} *server-info*]

         ;; check errors when using a bad endpoint
         (let [bad-endpoint "https://unknown.example.com/"
               context (i/instance bad-endpoint)
               response (<! (cimi/cloud-entry-point context))]
           #_(is context)
           (is (instance? #?(:clj Exception :cljs js/Error) response))
           (is (ex-data response)))

         ;; get the cloud entry point for working server
         (let [context (i/instance cep-endpoint)
               cep (<! (cimi/cloud-entry-point context))]
           #_(is context)
           #_(is (map? cep))
           #_(is (:baseURI cep))
           #_(is (:events cep))

           ;; try logging in with incorrect credentials
           (let [response (<! (authn/login context {:href  "session-template/internal"
                                                :username "UNKNOWN"
                                                :password "USER"}))]
             (is (instance? #?(:clj Exception :cljs js/Error) response))
             (is (= 403 (:status (ex-data response))))
             (is (false? (<! (authn/authenticated? context)))))

           ;; log into the server with correct credentials
           (let [response (<! (authn/login context {:href  "session-template/internal"
                                                :username username
                                                :password password}))]
             (is (= 201 (:status response)))
             (is (re-matches #"session/.+" (:resource-id response)))
             (is (true? (<! (authn/authenticated? context)))))

           ;; search for events (tests assume that real account with lots of events is used)
           (let [events (<! (cimi/search context "events" {:$first 10 :$last 20}))]
             (is (= 11 (count (:events events))))
             (is (pos? (:count events))))

           ;; add a new event resource
           (let [response (<! (cimi/add context "events" example-event))]
             (is (= 201 (:status response)))
             (is (re-matches #"event/.+" (:resource-id response)))

             ;; read the event back
             (let [event-id (:resource-id response)
                   read-event (<! (cimi/get context event-id))]
               (is (= (strip-fields example-event) (strip-fields read-event)))

               ;; events cannot be edited
               (let [edit-resp (<! (cimi/edit context event-id read-event))]
                 (is (instance? #?(:clj Exception :cljs js/Error) edit-resp)))

               ;; delete the event and ensure that it is gone
               (let [delete-resp (<! (cimi/delete context event-id))]
                 (is (= 200 (:status delete-resp)))
                 (is (re-matches #"event/.+" (:resource-id delete-resp)))
                 (let [get-resp (<! (cimi/get context event-id))]
                   (is (instance? #?(:clj Exception :cljs js/Error) get-resp))
                   (is (= 404 (:status (ex-data get-resp))))))))

           ;; logout from the server
           (let [logout-response (<! (authn/logout context))]
             (is (= 200 (:status logout-response)))
             (is (false? (<! (authn/authenticated? context)))))

           ;; try logging out again
           (let [logout-response (<! (authn/logout context))]
             (is (nil? logout-response))
             (is (false? (<! (authn/authenticated? context))))))))
     (if done (done)))))

(deftest check-event-lifecycle
  #?(:clj  (<!! (event-lifecycle))
     :cljs (async done (event-lifecycle done))))

