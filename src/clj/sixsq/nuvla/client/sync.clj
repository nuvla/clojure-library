(ns sixsq.nuvla.client.sync
  "A synchronous implementation of the CIMI protocol. All functions return the
   results as a clojure data structure.

   NOTE: The synchronous version of the API is **only available in Clojure**."
  (:refer-clojure :exclude [get])
  (:require
    [clojure.core.async]
    [sixsq.nuvla.client.authn :as authn]
    [sixsq.nuvla.client.api :as api]
    [sixsq.nuvla.client.async :as async]))


(defmacro ^{:no-doc true} <??
  "Extracts a value from the channel with <??. If the value is a Throwable, it
   throws the value; otherwise it simply returns it."
  [ch]
  `(let [v# (clojure.core.async/<!! ~ch)]
     (if (instance? Throwable v#)
       (throw v#)
       v#)))


(deftype cimi-sync [async-context]

  authn/authn

  (login [_ creds]
    (<?? (authn/login async-context creds)))
  (login [_ creds options]
    (<?? (authn/login async-context creds options)))
  (logout [_]
    (<?? (authn/logout async-context)))
  (logout [_ options]
    (<?? (authn/logout async-context options)))
  (authenticated? [_]
    (<?? (authn/authenticated? async-context)))
  (authenticated? [_ options]
    (<?? (authn/authenticated? async-context options)))

  api/api

  (cloud-entry-point [_]
    (<?? (api/cloud-entry-point async-context)))
  (cloud-entry-point [_ options]
    (<?? (api/cloud-entry-point async-context options)))
  (add [_ resource-type data]
    (<?? (api/add async-context resource-type data)))
  (add [_ resource-type data options]
    (<?? (api/add async-context resource-type data options)))
  (edit [_ url-or-id data]
    (<?? (api/edit async-context url-or-id data)))
  (edit [_ url-or-id data options]
    (<?? (api/edit async-context url-or-id data options)))
  (delete [_ url-or-id]
    (<?? (api/delete async-context url-or-id)))
  (delete [_ url-or-id options]
    (<?? (api/delete async-context url-or-id options)))
  (get [_ url-or-id]
    (<?? (api/get async-context url-or-id)))
  (get [_ url-or-id options]
    (<?? (api/get async-context url-or-id options)))
  (search [_ resource-type]
    (<?? (api/search async-context resource-type nil)))
  (search [_ resource-type options]
    (<?? (api/search async-context resource-type options)))
  (operation [_ url-or-id operation]
    (<?? (api/operation async-context url-or-id operation)))
  (operation [_ url-or-id operation data]
    (<?? (api/operation async-context url-or-id operation data)))
  (operation [_ url-or-id operation data options]
    (<?? (api/operation async-context url-or-id operation data options)))
  )


(defn instance
  "A convenience function for creating a synchronous, concrete instance of the
   CIMI protocol. Use of this function is strongly preferred to the raw
   constructor (`->cimi-sync`).

   If the endpoint is not provided or is nil, the default endpoint will be
   used.

   Optionally, you may also provide a default set of options that will be
   applied to all requests. The supported option is:

     * `:insecure?` - a boolean value to turn off/on the SSL certificate
       checking. This defaults to false. This option is only effective when
       using Clojure.

   You can override your provided defaults by specifying options directly on
   the individual CIMI function calls."
  ([]
   (->cimi-sync (async/instance)))
  ([endpoint]
   (->cimi-sync (async/instance endpoint)))
  ([endpoint default-options]
   (->cimi-sync (async/instance endpoint default-options))))
