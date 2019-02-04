(ns sixsq.slipstream.client.api.deprecated-authn
  "Provides a utility to log into the SlipStream server and to recover an
  access token. No logout function is provided as the access can be removed by
  just destroying the token.

  NOTE: **This API is deprecated.** Use the supported API in the namespace
  sixsq.slipstream.client.api.authn.

  The library API functions in namespaces under **sixsq.slipstream.client.run-impl.lib**
  use dynamic context defined in this namespace.  To bootstrap
  the API with the context use [[login!]].  Each time
  when called, it alters the root of the context with the new
  authentication token obtained after performing basic authn
  with the username/password provided to [[login!]].

  `(with-context new-context (api-function ...))` can be used to
  rebind the global context to the new provided one for the time of
  the invocation of a specific API function. The
  new context can, for example, be obtained by

      {:serviceurl \"https://my.slipstream\"
       :cookie (login username password)}

  Full example

      (require '[sixsq.slipstream.client.api.authn :as a])
      (require '[sixsq.slipstream.client.api.lib.app :as p])
      (require '[sixsq.slipstream.client.api.lib.run :as r])

      (def my-slipstream
        {:serviceurl \"https://my.slipstream\"
         :cookie (a/login username password)}

      (def run-uuid
        (last
          (clojure.string/split
            (with-context my-slipstream (p/deploy \"my/app\")))))

      (with-context my-slipstream
        (r/get-run-info run-uuid))

  To contact server in an insecure mode (i.e. w/o checking the authenticity of
  the remote server) before calling login functions, re-set the root of the
  authentication context with

      (require '[sixsq.slipstream.client.api.authn :as a])

      (a/set-context! {:insecure? true})
      (a/login username password)

  or wrap the API call for the local rebinding of the authentication context as
  follows

      (a/with-context {:insecure? true}
        (a/login! username password))
  "
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go go-loop]]))
  (:require
    [clojure.core.async :refer #?(:clj  [<! go go-loop <!!]
                                  :cljs [<!])]
    [clojure.string :as s]
    [sixsq.slipstream.client.impl.utils.http-async :as http]
    [sixsq.slipstream.client.impl.utils.utils :as u]))

(def ^:const default-url "https://nuv.la")

(def ^:const login-resource "auth/login")

(defn to-login-url
  [service-url]
  (u/url-join [service-url login-resource]))

(def ^:const default-login-url (to-login-url default-url))

;;
;; Authentication context management for the namespaces in client.api.lib/*.
(def default-context {:serviceurl default-url
                      :insecure?  false})

(def ^:dynamic *context* default-context)

(defn select-context
  [context]
  (select-keys context [:serviceurl :username :password :cookie :insecure?]))

;;
#?(:clj
   (defn set-context!
     "Should be called to provide service URL and connection token.
     The following map is expected

         {:serviceurl \"https://nuv.la\"
          :cookie     \"cookie\"}"
     [context]
     (alter-var-root #'*context* (fn [_] (merge default-context (select-context context))))))

(defmacro with-context
  [context & body]
  `(binding [*context* (merge *context* ~context)] (do ~@body)))

(defn result-tuple [result]
  ((juxt :status #(get-in % [:headers :set-cookie])) result))

(defn extract-response [result]
  (if (instance? #?(:clj Exception :cljs js/Error) result)
    (if-let [data (ex-data result)]
      (result-tuple data)
      result)
    (result-tuple result)))

(defn login-async-with-status
  "Uses the given username and password to log into the SlipStream
   server.

   This method returns a channel that will contain the results as a
   map with the keys :login-status and :token.  Depending on the
   underlying HTTP client, the token may be nil even when the login
   request succeeded.

   If called without an explicit login-url, then the default on Nuvla
   is used.

   **FIXME**: Ideally the login-url should be discovered from the cloud
   entry point."
  ([username password]
   (login-async-with-status username password default-login-url))
  ([username password login-url]
   (let [data (str "authn-method=internal&username=" username "&password=" password)]
     (go
       (let [result (<! (http/post login-url {:content-type     "application/x-www-form-urlencoded"
                                              :follow-redirects false
                                              :throw-exceptions false
                                              :body             data
                                              :insecure?        (:insecure? *context*)}))]
         (extract-response result))))))

(defn logout-async-with-status
  "Removes cached credentials and tokens from the http client."
  ([]
   (logout-async-with-status default-login-url))
  ([logout-url]
   (go
     (let [result (<! (http/post logout-url {:follow-redirects false
                                             :throw-exceptions false
                                             :insecure?        (:insecure? *context*)}))]
       (first (extract-response result))))))

(defn login-async
  "Uses the given username and password to log into the SlipStream
   server.

   This method returns a channel that will contain the results as a
   tuple of the status and token (cookie).  Depending on the
   underlying HTTP client, the token may be nil even when the login
   request succeeded.

   If called without an explicit login-url, then the default on Nuvla
   is used.

   **FIXME**: Ideally the login-url should be discovered from the cloud
   entry point."
  ([username password]
   (login-async username password default-login-url))
  ([username password login-url]
   (let [data (str "authn-method=internal&username=" username "&password=" password)]
     (go
       (let [result (<! (http/post login-url {:content-type     "application/x-www-form-urlencoded"
                                              :follow-redirects false
                                              :body             data
                                              :insecure?        (:insecure? *context*)}))]
         (-> result :headers :set-cookie))))))

#?(:clj
   (defn login
     "Synchronous login to the server.  Directly returns the access token.
      Not available in clojurescript."
     ([username password]
      (login username password default-login-url))
     ([username password login-url]
      (<!! (login-async username password login-url)))))

#?(:clj
   (defn endpoint-from-url
     [url]
     (->> (s/split url #"/")
          (take 3)
          (filter #(not (= % "")))
          (s/join "//"))))

#?(:clj
   (defn login!
     "Synchronous login to the server.  Alters the root of the global dynamic
     authentication context used in the namespaces under
     **sixsq.slipstream.client.api.lib** to interact with the service.
     Returns the access token.
     Not available in clojurescript."
     ([username password]
      (login! username password default-login-url))
     ([username password login-url]
      (let [token (login username password login-url)]
        (set-context! (merge *context*
                             {:serviceurl (endpoint-from-url login-url)
                              :cookie     token}))
        token))))

