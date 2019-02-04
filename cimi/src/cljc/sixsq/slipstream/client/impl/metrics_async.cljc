(ns ^{:no-doc true} sixsq.slipstream.client.impl.metrics-async
  "Provides the core, low-level function for retrieving the metrics from the
   server. The CIMI Cloud Entry Point is used to find the baseURI for the
   server; the metrics are located at a fixed relative URL below that."
  (:require
    [clojure.core.async :refer #?(:clj  [chan]
                                  :cljs [chan])]
    [sixsq.slipstream.client.impl.utils.cimi :as u]
    [sixsq.slipstream.client.impl.utils.common :as cu]
    [sixsq.slipstream.client.impl.utils.http-async :as http]))


(def ^:const metrics-url "metrics")


(defn- create-chan
  "Creates a channel that extracts returns the JSON body as a keywordized EDN
   data structure and the value of the set-cookie header (if any). Any
   exceptions that occur in processing are pushed onto the channel also as a
   response/cookie tuple."
  []
  (chan 1 (u/response-xduce) u/error-tuple))


(defn- assoc-chan
  [m]
  (assoc m :chan (create-chan)))


(defn get-metrics
  "Reads the metrics document from the server. Returns the resource as an edn
   data structure in a channel."
  [{:keys [token cep] :as state} options]
  (let [url (cu/ensure-url (:baseURI cep) metrics-url)]
    (let [opts (-> (cu/req-opts token)
                   (merge options)
                   assoc-chan)]
      (http/get url opts))))
