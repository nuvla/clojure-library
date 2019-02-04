(ns ^{:no-doc true} sixsq.slipstream.client.impl.runs-async
  "Provides the core, low-level functions for SCRUD actions on run resources.
   These are details of the implementation and are not a part of the public
   API."
  (:require
    [cemerick.url :as url]
    [clojure.core.async :refer [chan]]
    [sixsq.slipstream.client.impl.utils.common :as cu]
    [sixsq.slipstream.client.impl.utils.http-async :as http]
    [sixsq.slipstream.client.impl.utils.json :as json]
    [sixsq.slipstream.client.impl.utils.run-params :as rp-utils]))


(defn- create-chan
  "Creates a channel that extracts the JSON body and then transforms the body
   into a clojure data structure with keywordized keys. Any exceptions that
   occur in processing are pushed onto the channel."
  []
  (chan 1 (json/body-as-json) identity))


(defn get-run
  "Reads the run identified by the URL or resource id. Returns the run as an
   edn data structure in a channel."
  [token endpoint url-or-id {:keys [insecure?] :as options}]
  (let [url (cu/ensure-url-slash endpoint url-or-id)
        opts (-> (cu/req-opts token)
                 (assoc :insecure? insecure?)
                 (assoc :chan (create-chan)))]
    (http/get url opts)))


(defn start-run
  "Start a run from the module identified by its URI."
  [token endpoint uri {:keys [insecure?] :as options}]
  (let [query-string (-> options
                         (dissoc :insecure?)
                         rp-utils/parse-params
                         (rp-utils/assoc-module-uri uri)
                         url/map->query)
        opts (-> (cu/req-opts token query-string)
                 (assoc :type "application/x-www-form-urlencoded")
                 (assoc :insecure? insecure?)
                 (assoc :chan (chan 1 (rp-utils/extract-location) identity)))]
    (http/post endpoint opts)))


(defn terminate-run
  "Terminates the run identified by the URL or resource id."
  [token endpoint url-or-id {:keys [insecure?] :as options}]
  (let [url (cu/ensure-url-slash endpoint url-or-id)
        opts (-> (cu/req-opts token)
                 (assoc :insecure? insecure?)
                 (assoc :chan (chan 1 identity identity)))]
    (http/delete url opts)))


(defn- query-params [options]
  (select-keys options #{:cloud :activeOnly :offset :limit}))


(defn search-runs
  "Search for CIMI resources of the given type, returning a list of the
   matching resources (in a channel). The list will be wrapped within
   an envelope containing the metadata of the collection and search."
  [token endpoint {:keys [insecure?] :as options}]
  (let [opts (-> (cu/req-opts token)
                 (assoc :query-params (query-params options))
                 (assoc :insecure? insecure?)
                 (assoc :chan (create-chan)))]
    (http/get endpoint opts)))
