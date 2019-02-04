(ns ^{:no-doc true} sixsq.slipstream.client.impl.utils.http-sync
  "Simple synchronous wrapper around an HTTP library to produce consistent
  CRUD interface.

  The CRUD actions accept and produce `Ring`-style requests/responses.

  Examples of the request/response.

  ```
  GET using Basic authn
  req := {:accept :xml
          :basic-auth [\"user\" \"password\"]}
  resp := {:status 200
           :body \"<Hello :)>\"
           :headers {...}}
  ```

  ```
  GET using cookie
  req := {:accept :json
          :headers {:cookie cookie}}
  resp := {:status 200
           :body \"{\"Hello\": \":)\"}\"
           :headers {...}}
  ```

  ```clojure
  (:body (get \"https://httpbin.org/get\"))
  ```

  On HTTP error, throws ExceptionInfo with `:data` containing the full response.
  The response can be obtained with `(ex-data ex)`

  ```clojure
  (let [{:keys [status body]}
        (try
          (get \"https://httpbin.org/error\")
          (catch ExceptionInfo e (ex-data e)))
  ```
  "
  (:refer-clojure :exclude [get])
  (:require
    [kvlt.core :as kvlt]
    [sixsq.slipstream.client.impl.utils.http-utils :as hu]))

(defn- re-throw-ex-info
  [e]
  (let [data (ex-data (.getCause e))
        body (or (:body data) "")
        msg (str "HTTP Error: " (:status data) ". " body)]
    (throw (ex-info msg data))))

(defn- request!
  "Synchronous request.  Throws `ExecutionInfo` on HTTP errors
   with `:data` as Ring-style response.
   To extract the response on error, catch `ExecutionInfo` and call
   `(ex-data e)`."
  [meth url req]
  (try
    @(kvlt/request!
       (merge {:method (keyword meth) :url url} (hu/set-or-clear-insecure-flag req)))
    (catch java.util.concurrent.ExecutionException e (re-throw-ex-info e))))

(defn get
  [url & [req]]
  (request! :get url req))

(defn put
  [url & [req]]
  (request! :put url req))

(defn post
  [url & [req]]
  (request! :post url req))

(defn delete
  [url & [req]]
  (request! :delete url req))
