(ns sixsq.slipstream.client.api.cimi
  (:refer-clojure :exclude [get]))

(defprotocol cimi
  "This protocol (interface) defines all the SCRUD (search, create, read,
   update, and delete) actions for CIMI resources. It also defines convenience
   functions for authenticating with the server and a function to execute
   specialized operations on a given resource or collection.

   To use the protocol you must instantiate a concrete implementation of the
   protocol. Synchronous and asynchronous implementations are available in the
   following namespaces:

    * `sixsq.slipstream.client.async`
    * `sixsq.slipstream.client.sync`

   and can be created easily via the `instance` function. Note that the
   concrete return types will depend on the implementation. The asynchronous
   implementation, for example, returns core.async channels from all functions.

   All functions take an optional options map.  All functions support:

    * `:insecure?` - Will not check the validity of SSL certificates if true.
      Defaults to false. The option is only effective with the Clojure
      implementation.

   Options for individual functions are noted in the function descriptions.
   Unknown options for any function are silently ignored.
   "

  (cloud-entry-point
    [this]
    [this options]
    "Retrieves the cloud entry point from the server. The cloud entry point
     (CEP) acts as a directory of the available resources within the CIMI
     server. This function does not require authentication. The result is
     returned in EDN format. Implementations may cache the cloud entry point to
     avoid unnecessary requests to the server.")

  (add
    [this resource-type data]
    [this resource-type data options]
    "Creates a new CIMI resource of the given type. The data will be converted
     into a JSON string before being sent to the server. The data must match
     the schema of the resource type. Returns a map with the status, message,
     and created resource-id.")

  (edit
    [this url-or-id data]
    [this url-or-id data options]
    "Updates an existing CIMI resource identified by the URL or resource id.
     The data must be the complete, updated data of the resource. Returns the
     updated resource in EDN format.")

  (delete
    [this url-or-id]
    [this url-or-id options]
    "Deletes the CIMI resource identified by the URL or resource id from the
     server. Returns a map with a status and message.")

  (get
    [this url-or-id]
    [this url-or-id options]
    "Reads the CIMI resource identified by the URL or resource id. Returns the
     resource as EDN data. This function also supports the options:

      * `:sse?` - If set to true, the function will return an event stream of
        results. The default is false. Only effective asynchronous
        implementations.
      * `:events` - A set of events to accept when SSE is requested. You can
        select all events by adding `:*` to the set. By default, only
        `:message` events are returned.")

  (search
    [this resource-type]
    [this resource-type options]
    "Search for CIMI resources of the given type, returning a list of the
     matching resources. The list will be wrapped within an envelope containing
     the metadata of the collection and search. The returned document is in EDN
     format.

     This function also supports all of the CIMI query options: `:$first`,
     `:$last`, `:$filter`, `:$orderby`, `:$select`, `:$aggregation`. It also
     supports the `:sse?` and `:events` options described in the `get` function
     description.")

  (operation
    [this url-or-id operation]
    [this url-or-id operation data]
    [this url-or-id operation data options]
    "Executes the chosen operation on the resource identified by the
     url-or-id. The operation must specify the full URI. If the data is
     provided, then it will be sent as a JSON document as the body of the POST
     request. The data is passed through to the server without any validity
     checks. The function will return a map with the results of the function
     call."))
