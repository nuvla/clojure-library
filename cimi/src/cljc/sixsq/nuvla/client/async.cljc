(ns sixsq.nuvla.client.async
  "An asynchronous implementation of the CIMI protocol that returns core.async
   channels from all functions. The returned channel contains a single result
   (or error message) unless an event stream was requested."
  (:refer-clojure :exclude [get])
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go]]))
  (:require
    [clojure.core.async :refer #?(:clj  [chan >! <! go]
                                  :cljs [chan >! <!])]
    [sixsq.nuvla.client.api.authn :as authn]
    [sixsq.nuvla.client.api.cimi :as cimi]
    [sixsq.nuvla.client.api.metrics :as metrics]
    [sixsq.nuvla.client.impl.cimi-async :as cimi-impl]
    [sixsq.nuvla.client.impl.metrics-async :as metrics-impl]
    [sixsq.nuvla.client.impl.utils.cimi :as u]))

(def
  ^{:doc "Default cloud entry point endpoint defaults to the Nuvla service."}
  default-cep-endpoint "https://nuv.la/api/cloud-entry-point")

(deftype cimi-async [endpoint state]

  authn/authn

  (login
    [this login-params]
    (authn/login this login-params nil))
  (login
    [this login-params options]
    (let [opts (merge (:default-options @state) options)]
      (go
        (<! (cimi/cloud-entry-point this opts))
        (let [[response token] (<! (cimi-impl/login @state login-params opts))]
          (u/update-state state :token token)
          response))))

  (logout
    [this]
    (authn/logout this nil))
  (logout
    [this options]
    (let [opts (merge (:default-options @state) options)]
      (go
        (<! (cimi/cloud-entry-point this opts))
        (let [[response token] (<! (cimi-impl/logout @state opts))]
          (u/update-state state :token token)
          response))))

  (authenticated?
    [this]
    (authn/authenticated? this nil))
  (authenticated?
    [this options]
    (let [opts (merge (:default-options @state) options)]
      (go
        (<! (cimi/cloud-entry-point this opts))
        (let [[session-id _] (<! (cimi-impl/current-session @state opts))]
          (not (nil? session-id))))))

  cimi/cimi

  (cloud-entry-point
    [this]
    (cimi/cloud-entry-point this nil))
  (cloud-entry-point
    [_ options]
    (go
      (or (:cep @state)
          (let [opts (merge (:default-options @state) options)
                [cep token] (<! (cimi-impl/cloud-entry-point endpoint opts))]
            (u/update-state state :token token)
            (u/update-state state :cep cep)
            cep))))

  (add [this resource-type data]
    (cimi/add this resource-type data nil))
  (add [this resource-type data options]
    (go
      (<! (cimi/cloud-entry-point this options))
      (let [opts (merge (:default-options @state) options)
            [response token] (<! (cimi-impl/add @state resource-type data opts))]
        (u/update-state state :token token)
        response)))

  (edit [this url-or-id data]
    (cimi/edit this url-or-id data nil))
  (edit [this url-or-id data options]
    (let [opts (merge (:default-options @state) options)]
      (go
        (<! (cimi/cloud-entry-point this opts))
        (let [[response token] (<! (cimi-impl/edit @state url-or-id data opts))]
          (u/update-state state :token token)
          response))))

  (delete [this url-or-id]
    (cimi/delete this url-or-id nil))
  (delete [this url-or-id options]
    (let [opts (merge (:default-options @state) options)]
      (go
        (<! (cimi/cloud-entry-point this opts))
        (let [[response token] (<! (cimi-impl/delete @state url-or-id opts))]
          (u/update-state state :token token)
          response))))

  (get [this url-or-id]
    (cimi/get this url-or-id nil))
  (get [this url-or-id {:keys [sse?] :as options}]
    (let [opts (merge (:default-options @state) options)]
      (if sse?
        (cimi-impl/get-sse @state url-or-id opts)
        (go
          (<! (cimi/cloud-entry-point this opts))
          (let [[response token] (<! (cimi-impl/get @state url-or-id opts))]
            (u/update-state state :token token)
            response)))))

  (search [this resource-type]
    (cimi/search this resource-type nil))
  (search [this resource-type {:keys [sse?] :as options}]
    (let [opts (merge (:default-options @state) options)]
      (if sse?
        (cimi-impl/search-sse @state resource-type opts)
        (go
          (<! (cimi/cloud-entry-point this opts))
          (let [[response token] (<! (cimi-impl/search @state resource-type opts))]
            (u/update-state state :token token)
            response)))))

  (operation [this url-or-id operation]
    (cimi/operation this url-or-id operation nil nil))
  (operation [this url-or-id operation data]
    (cimi/operation this url-or-id operation data nil))
  (operation [this url-or-id operation data options]
    (let [opts (merge (:default-options @state) options)]
      (go
        (<! (cimi/cloud-entry-point this opts))
        (let [[response token] (<! (cimi-impl/operation @state url-or-id operation data opts))]
          (u/update-state state :token token)
          response))))

  metrics/metrics

  (get-metrics [this options]
    (let [opts (merge (:default-options @state) options)]
      (go
        (<! (cimi/cloud-entry-point this opts))
        (let [[response token] (<! (metrics-impl/get-metrics @state opts))]
          (u/update-state state :token token)
          response)))))


(defn instance
  "A convenience function for creating an asynchronous, concrete instance of
   the CIMI protocol. All the CIMI functions for this client return a
   core.async channel. Use of this function is strongly preferred to the raw
   constructor (`->cimi-async`).

   If the endpoint is not provided or is nil, the default endpoint will be
   used.

   Optionally, you may also provide a default set of options that will be
   applied to all requests. The supported options are:

     * `:insecure?` - a boolean value to turn off/on the SSL certificate
       checking. This defaults to false. This option is only effective when
       using Clojure.
     * `:sse?` - a boolean value to indicate that a channel of Server Sent
       Events should be returned. Defaults to false. This option is only
       effective for the `get` and `search` functions.
     * `:events` - a set of message types to return when `:sse?` is true. You
       can select all messages by adding `:*` to the set. The default is to
       only accept `:message` events.

   You can override your provided defaults by specifying options directly on
   the individual CIMI function calls."
  ([]
   (instance nil nil))
  ([cep-endpoint]
   (instance cep-endpoint nil))
  ([cep-endpoint default-options]
   (let [defaults {:insecure? false
                   :sse?      false}
         endpoint (or cep-endpoint default-cep-endpoint)]
     (->cimi-async endpoint
                   (atom {:default-options (merge defaults default-options)})))))
