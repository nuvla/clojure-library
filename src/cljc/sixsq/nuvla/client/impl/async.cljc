(ns ^{:no-doc true} sixsq.nuvla.client.impl.async
  "Provides the core, low-level functions for SCRUD actions on Nuvla resources.
   These are details of the implementation and are not a part of the public
   API.

   Unless otherwise stated, all functions in this namespace return a
   core.async channel with the function's result.

   Most of these functions will return (on a core.async channel) a tuple
   containing the request response and any cookie/token provided by the server
   in the set-cookie header. For ClojureScript, the cookie/token may be nil
   even for successful requests because of JavaScript security protections in
   the browser."
  (:refer-clojure :exclude [get])
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go]]))
  (:require
    [cemerick.url :as url]
    [clojure.core.async :refer #?(:clj  [chan <! >! go]
                                  :cljs [chan <! >!])]
    [sixsq.nuvla.client.impl.utils.cimi :as u]
    [sixsq.nuvla.client.impl.utils.common :as cu]
    [sixsq.nuvla.client.impl.utils.http-async :as http]
    [sixsq.nuvla.client.impl.utils.json :as json]))


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


(defn- create-sse-chan
  "Creates a channel that extracts the data from an SSE message and returns
   the JSON body as a keywordized EDN data structure."
  []
  (chan 1 (u/event-transducer) identity))


(defn- assoc-sse-chan
  [m]
  (assoc m :chan (create-sse-chan)))


(defn- create-op-url-chan
  "Creates a channel that extracts the operations from a collection or
   resource."
  [op base-uri]
  (chan 1 (u/extract-op-url op base-uri) identity))


(defn- assoc-op-url-chan
  [m op base-uri]
  (assoc m :chan (create-op-url-chan op base-uri)))


(defn get-collection-op-url
  "Returns the URL for the given operation and collection within a channel.
   The collection can be identified either by its name or URL."
  [token {:keys [base-uri] :as cep} op collection-name-or-url options]
  (let [url (or (u/get-collection-url cep collection-name-or-url)
                (u/verify-collection-url cep collection-name-or-url))
        opts (-> (cu/req-opts token (url/map->query {"last" 0}))
                 (merge options)
                 (assoc :type "application/x-www-form-urlencoded")
                 (assoc-op-url-chan op base-uri))]
    (http/put url opts)))


(defn get-resource-op-url
  "Returns the URL for the given operation and collection within a channel."
  [{:keys [token cep] :as state} op url-or-id options]
  (let [base-uri (:base-uri cep)
        url (cu/ensure-url base-uri url-or-id)
        opts (-> (cu/req-opts token)
                 (merge options)
                 (assoc-op-url-chan op base-uri))]
    (http/get url opts)))


(defn add
  "Creates a new resource within the collection identified by the
   collection type or URL. The data will be converted into a JSON string before
   being sent to the server. The data must match the schema of the resource."
  [{:keys [token cep] :as state} collection-type-or-url data options]
  (go
    (if-let [add-url (<! (get-collection-op-url token cep "add" collection-type-or-url options))]
      (let [opts (-> (cu/req-opts token (json/edn->json data))
                     (merge options)
                     assoc-chan)]
        (<! (http/post add-url opts)))
      (u/unauthorized collection-type-or-url))))


(defn edit
  "Updates an existing resource identified by the URL or resource id."
  [{:keys [token cep] :as state} url-or-id data options]
  (go
    (if-let [edit-url (<! (get-resource-op-url state "edit" url-or-id options))]
      (let [opts (-> (cu/req-opts token (json/edn->json data))
                     (merge options)
                     assoc-chan)]
        (<! (http/put edit-url opts)))
      (u/unauthorized url-or-id))))


(defn delete
  "Deletes the resource identified by the URL or resource id from the
   server."
  [{:keys [token cep] :as state} url-or-id options]
  (go
    (if-let [delete-url (<! (get-resource-op-url state "delete" url-or-id options))]
      (let [opts (-> (cu/req-opts token)
                     (merge options)
                     assoc-chan)]
        (<! (http/delete delete-url opts)))
      (u/unauthorized url-or-id))))


(defn get
  "Reads the resource identified by the URL or resource id. Returns the
   resource as an edn data structure in a channel."
  [{:keys [token cep] :as state} url-or-id options]
  (let [url (cu/ensure-url (:base-uri cep) url-or-id)]
    (let [opts (-> (cu/req-opts token)
                   (merge options)
                   assoc-chan)]
      (http/get url opts))))


(defn get-sse
  "Reads the resource identified by the URL or resource id. Returns the
   resource as an edn data structure in a channel."
  [{:keys [token cep] :as state} url-or-id options]
  (http/sse (cu/ensure-url (:base-uri cep) url-or-id)
            (cond-> (assoc-sse-chan options)
                    token (assoc :options {:headers {:cookie token}}))))


(defn search
  "Search for resources within the collection identified by its type or
   URL, returning a list of the matching resources (in a channel). The list
   will be wrapped within an envelope containing the metadata of the collection
   and search."
  [{:keys [token cep] :as state} collection-type-or-url {:keys [insecure? user-token] :as options}]
  (let [url (or (u/get-collection-url cep collection-type-or-url)
                (u/verify-collection-url cep collection-type-or-url))
        token-request (if user-token user-token token)
        query-string (url/map->query (u/select-cimi-params options))]
    (let [query-params (-> options
                           u/remove-cimi-params
                           u/remove-req-params)
          opts (-> (cu/req-opts token-request query-string)
                   (assoc :type "application/x-www-form-urlencoded")
                   (assoc :query-params query-params)
                   (assoc :insecure? insecure?)
                   assoc-chan)]
      (http/put url opts))))


(defn search-sse
  "Search for resources within the collection identified by its type or
   URL, returning a list of the matching resources (in a channel). The list
   will be wrapped within an envelope containing the metadata of the collection
   and search."
  [{:keys [token cep] :as state} collection-type-or-url options]
  (let [url (or (u/get-collection-url cep collection-type-or-url)
                (u/verify-collection-url cep collection-type-or-url))
        query-string (url/map->query (u/select-cimi-params options))]
    (http/sse (str url "?" query-string)
              (cond-> (assoc-sse-chan (u/remove-cimi-params options))
                      token (assoc :options {:headers {:cookie token}})))))


(defn delete-bulk
  "Bulk delete for resources within the collection identified by its type or
   URL, returning result metadata of the operation (in a channel)."
  [{:keys [token cep] :as state} collection-type-or-url filter
   {:keys [insecure? user-token] :as options}]
  (let [url (or (u/get-collection-url cep collection-type-or-url)
                (u/verify-collection-url cep collection-type-or-url))
        token-request (if user-token user-token token)
        query-string (url/map->query {:filter filter})]
    (let [query-params (-> options
                           u/remove-cimi-params
                           u/remove-req-params)
          opts (-> (cu/req-opts token-request query-string)
                   (assoc :type "application/x-www-form-urlencoded")
                   (assoc :query-params query-params)
                   (assoc :insecure? insecure?)
                   (assoc-in [:headers :bulk] "yes")
                   assoc-chan)]
      (http/delete url opts))))


(defn operation
  "Reads the resource identified by the URL or resource id and then
   'executes' the given operation."
  [{:keys [token] :as state} url-or-id operation data options]
  (go
    (if-let [operation-url (<! (get-resource-op-url state operation url-or-id options))]
      (let [opts (-> (cu/req-opts token (json/edn->json data))
                     (merge options)
                     assoc-chan)]
        (<! (http/post operation-url opts)))
      (u/unknown-operation url-or-id))))


(defn cloud-entry-point
  "Retrieves the cloud entry point from the given endpoint. The cloud entry
   point acts as a directory of the available resources within the server.
   This returns a channel which will contain the cloud entry point in edn
   format."
  [endpoint options]
  (let [opts (-> (cu/req-opts)
                 (merge options)
                 assoc-chan)]
    (http/get endpoint opts)))


(defn current-session
  "Returns (on a channel) the resource ID of the current session. If there is
   no current session (user is not logged in) or an error occurs, then nil will
   be returned on the channel."
  [state options]
  (go
    (let [[sessions token] (<! (search state :session options))]
      [(-> sessions :resources first :id) token])))


(defn logout
  "Logs out a user by sending a DELETE request for the current session. The
   function returns a tuple with the request response and the token passed back
   from the server (typically a cookie invalidating any previous one). The
   method will return nil if there was no current session."
  [state options]
  (go
    (let [[session-id _] (<! (current-session state options))]
      (when session-id
        (<! (delete state session-id options))))))


(defn login
  "Creates a session create template from the provided login parameters and
   posts this to the session collection to create a new session. Returns a
   tuple with the request response and the authentication token (if any) given
   by the server."
  ([state login-params]
   (login state login-params nil))
  ([state login-params options]
   (add state :session {:template login-params} options)))


