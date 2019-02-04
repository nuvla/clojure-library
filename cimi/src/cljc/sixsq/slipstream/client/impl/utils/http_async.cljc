(ns ^{:no-doc true} sixsq.slipstream.client.impl.utils.http-async
  "Asynchronous wrapper around standard HTTP calls to provide a uniform interface.

  All actions accept requests in Ring-like format and return a channel.  All results
  and errors are placed on the returned channel."
  (:refer-clojure :exclude [get])
  (:require
    [kvlt.chan :as kvlt-chan]
    [kvlt.core :as kvlt]))

(defn- request-async!
  [meth url {:keys [insecure? chan] :as req}]
  (kvlt-chan/request! (-> req
                          (assoc :method (keyword meth))
                          (assoc :url url)
                          (assoc :kvlt.platform/insecure? insecure?)
                          (dissoc :insecure?)
                          (dissoc :chan))
                      {:chan chan}))

(defn- request-event-source!
  [url {:keys [insecure? options] :as opts}]
  (let [updated-options (assoc options :kvlt.platform/insecure? insecure?)]
    (kvlt/event-source! url (-> opts
                                (dissoc :sse?)
                                (dissoc :insecure?)
                                (assoc :options updated-options)))))

(defn get
  [url & [req]]
  (request-async! :get url req))

(defn put
  [url & [req]]
  (request-async! :put url req))

(defn post
  [url & [req]]
  (request-async! :post url req))

(defn delete
  [url & [req]]
  (request-async! :delete url req))

(defn sse
  [url & [{:keys [options] :as opts}]]
  (request-event-source! url opts))
