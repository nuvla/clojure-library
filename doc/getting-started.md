# Getting Started

## Introduction

Nuvla provides an application management platform for edge computing
infrastructures. This library facilitates use of the Nuvla API from
the Clojure programming language.

The Nuvla API is inspired by the
[CIMI](https://www.dmtf.org/standards/cloud) standard from
[DMTF](http://dmtf.org).  For ease of use and consistency, there are
numerous differences between the Nuvla API and the CIMI standard.
Consequently, this library will not function as a general CIMI API
client.

## Installation

Install the Nuvla clojure library jar file as a dependency of your
project. For leiningen, for example, include the following dependency:

```
[sixsq.nuvla/clojure-library]
```

The primary namespaces that you will use are:

 - `sixsq.nuvla.client.api`, which contains the protocols for all the
   clients,

 - `sixsq.nuvla.client.async`, an asynchronous client that implements
   the API protocols,

 - `sixsq.nuvla.client.sync`, a synchronous client that implements the
   API protocols (Clojure only).

You will need to have access to a running Nuvla platform and an
account on that platform.

## Usage

The search (search), create (add), read (get), update (edit), and
delete (delete) operations are defined in the `api` protocol in the
`sixsq.nuvla.client.api` namespace.  A separate namespace
`sixsq.nuvla.client.authn` wraps some of the functions to simplify the
authentication process.

To use the api and authn protocol functions, you must instantiate
either a synchronous or asynchronous implementation of those
protocols.  The synchronous implementation directly returns the
results of the functions, while the asynchronous implementation always
returns a core.async channel.

```clojure
;; Demonstrating use from the REPL

(require '[sixsq.nuvla.client.api :as api])
(require '[sixsq.nuvla.client.authn :as authn])
(require '[sixsq.nuvla.client.async :as async])
(require '[sixsq.nuvla.client.sync :as sync])
(require '[clojure.core.async :refer [<!! close!]])
(require '[kvlt.core :as kvlt])

;; Turn off logging from underlying HTTP library to keep the noise down.
(kvlt/quiet!)

;; Create an asynchronous client context.  The asynchronous client
;; can be used from Clojure or ClojureScript.
(def client-async (async/instance))

;; Returns a channel on which a document with directory of available
;; resource is pushed. User does not need to be authenticated.
(pprint (<!! (api/cloud-entry-point client-async)))
;; {:baseURI "https://nuvla.io/api/",
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
(pprint (<!! (api/search client-async "events")))

;; {:count 7254,
;;  :resource-type "event-collection",
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

(pprint (api/search client-sync "events" {:$last 2}))

;; {:count 7254,
;;  :resource-type "event-collection",
;;  :id "event",
;;  ...

```

When creating the client context without specific endpoints, then the
endpoints for the public Nuvla service will be used.  See the API
documentation for details on specifying the endpoints or other
options.
