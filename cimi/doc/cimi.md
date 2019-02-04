# CIMI API

The SlipStream API is migrating from a custom REST API to the
[CIMI](https://www.dmtf.org/standards/cloud) standard from
[DMTF](http://dmtf.org).  All recent additions to the resource model
already follow the CIMI REST interface.

## Usage

The CIMI search (search), create (add), read (get), update (edit), and
delete (delete) operations are defined in the `cimi` protocol in the
`sixsq.slipstream.client.api.cimi` namespace.  A separate namespace 
`sixsq.slipstream.client.api.authn` wraps some of the CIMI functions to
simplify the authentication process. 

To use the CIMI and authn protocol functions, you must instantiate either
a synchronous or asynchronous implementation of those protocols.  The
synchronous implementation directly returns the results of the
functions, while the asynchronous implementation always returns a
core.async channel.

```clojure
;; Demonstrating use from the REPL

(require '[sixsq.slipstream.client.api.cimi :as cimi])
(require '[sixsq.slipstream.client.api.authn :as authn])
(require '[sixsq.slipstream.client.async :as async])
(require '[sixsq.slipstream.client.sync :as sync])
(require '[clojure.core.async :refer [<!! close!]])
(require '[kvlt.core :as kvlt])

;; Turn off logging from underlying HTTP library to keep the noise down.
(kvlt/quiet!)

;; Create an asynchronous client context.  The asynchronous client
;; can be used from Clojure or ClojureScript.
(def client-async (async/instance))
;; #'boot.user/client-async

;; Returns a channel on which a document with directory of available
;; resource is pushed. User does not need to be authenticated.
(pprint (<!! (cimi/cloud-entry-point client-async)))
;; {:baseURI "https://nuv.la/api/",
;;  :connectors {:href "connector"},
;; ...


;; Returns a channel with login status (HTTP code).
(def username "user") ;; replace with real value
(def password "pass") ;; replace with real value
(pprint (<!! (authn/login client-async {:href "session-template/internal"
                                        :username username
                                        :password password})))
;; {:status 201,
;;  :message "created session/884000e0-94a2-44af-9598-135060347557",
;;  :resource-id "session/884000e0-94a2-44af-9598-135060347557"}

;; Returns channel with document containing list of events.
(pprint (<!! (cimi/search client-async "events")))

;; {:count 7254,
;;  :resourceURI "http://schemas.dmtf.org/cimi/2/EventCollection",
;;  :id "event",
;;  ...


;; Same can be done with synchronous client, but in this case
;; all values are directly returned, rather than being pushed
;; onto a channel.  The synchronous client is only available 
;; in Clojure!

(def client-sync (sync/instance))
(pprint (authn/login client-sync {:href "session-template/internal"
                                  :username username
                                  :password password}))
                                  
;; {:status 201,
;;  :message "created session/45824c04-d454-4474-b3e6-4fdbb6389613",
;;  :resource-id "session/45824c04-d454-4474-b3e6-4fdbb6389613"}

(pprint (cimi/search client-sync "events" {:$last 2}))

;; {:count 7254,
;;  :resourceURI "http://schemas.dmtf.org/cimi/2/EventCollection",
;;  :id "event",
;;  ...

```

When creating the client context without specific endpoints, then
the endpoints for the Nuvla service will be used.  See the API
documentation for details on specifying the endpoints or other
options. 
