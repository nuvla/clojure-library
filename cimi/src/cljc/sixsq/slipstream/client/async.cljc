(ns sixsq.slipstream.client.async
  "An asynchronous implementation of the CIMI protocol that returns core.async
   channels from all functions. The returned channel contains a single result
   (or error message) unless an event stream was requested."
  (:refer-clojure :exclude [get])
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go]]))
  (:require
    [clojure.core.async :refer #?(:clj  [chan >! <! go]
                                  :cljs [chan >! <!])]
    [sixsq.slipstream.client.api.authn :as authn]
    [sixsq.slipstream.client.api.cimi :as cimi]
    [sixsq.slipstream.client.api.metrics :as metrics]
    [sixsq.slipstream.client.api.modules :as modules]
    [sixsq.slipstream.client.api.pricing :as pricing]
    [sixsq.slipstream.client.api.runs :as runs]
    [sixsq.slipstream.client.impl.cimi-async :as cimi-impl]
    [sixsq.slipstream.client.impl.metrics-async :as metrics-impl]
    [sixsq.slipstream.client.impl.modules-async :as modules-impl]
    [sixsq.slipstream.client.impl.pricing-async :as pi]
    [sixsq.slipstream.client.impl.runs-async :as runs-impl]
    [sixsq.slipstream.client.impl.utils.cimi :as u]
    [sixsq.slipstream.client.impl.utils.modules :as modules-utils]))

(def
  ^{:doc "Default cloud entry point endpoint defaults to the Nuvla service."}
  default-cep-endpoint "https://nuv.la/api/cloud-entry-point")

(deftype cimi-async [endpoint modules-endpoint runs-endpoint state]

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

  pricing/pricing

  (place-and-rank [this module-uri connectors]
    (pricing/place-and-rank this module-uri connectors nil))
  (place-and-rank [this module-uri connectors options]
    (go
      (let [opts (merge (:default-options @state) options)
            {:keys [baseURI]} (<! (cimi/cloud-entry-point this))
            endpoint (second (re-matches #"^(https?://[^/]+)/.*$" baseURI))]
        (<! (pi/place-and-rank @state endpoint module-uri connectors opts)))))

  modules/modules

  (get-module [this url-or-id]
    (modules/get-module this url-or-id nil))
  (get-module [this url-or-id options]
    (go
      (let [opts (merge (:default-options @state) options)
            token (:token @state)]
        (<! (modules-impl/get-module token modules-endpoint url-or-id opts)))))

  (get-module-children [this url-or-id]
    (modules/get-module-children this url-or-id nil))
  (get-module-children [this url-or-id options]
    (go
      (let [opts (merge (:default-options @state) options)
            token (:token @state)]
        (if url-or-id
          (let [module (<! (modules-impl/get-module token modules-endpoint url-or-id opts))]
            (modules-utils/extract-children module))
          (let [root (<! (modules-impl/get-module token modules-endpoint nil opts))]
            (modules-utils/extract-root-children root))))))

  runs/runs

  (get-run [this url-or-id]
    (runs/get-run this url-or-id nil))
  (get-run [this url-or-id options]
    (go
      (let [opts (merge (:default-options @state) options)
            token (:token @state)]
        (<! (runs-impl/get-run token runs-endpoint url-or-id opts)))))

  (start-run [this url-or-id]
    (runs/start-run this url-or-id nil))
  (start-run [this uri options]
    (go
      (let [opts (assoc options :insecure? (:insecure? (:default-options @state)))
            token (:token @state)]
        (<! (runs-impl/start-run token runs-endpoint uri opts)))))

  (terminate-run [this url-or-id]
    (runs/terminate-run this url-or-id nil))
  (terminate-run [this url-or-id options]
    (go
      (let [opts (merge (:default-options @state) options)
            token (:token @state)]
        (<! (runs-impl/terminate-run token runs-endpoint url-or-id opts)))))

  (search-runs [this]
    (runs/search-runs this nil))
  (search-runs [this options]
    (go
      (let [opts (merge (:default-options @state) options)
            token (:token @state)]
        (<! (runs-impl/search-runs token runs-endpoint opts)))))

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
         endpoint (or cep-endpoint default-cep-endpoint)
         modules-endpoint (u/legacy-endpoint endpoint "module")
         runs-endpoint (u/legacy-endpoint endpoint "run")]
     (->cimi-async endpoint
                   modules-endpoint
                   runs-endpoint
                   (atom {:default-options (merge defaults default-options)})))))
