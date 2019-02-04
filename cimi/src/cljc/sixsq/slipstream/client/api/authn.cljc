(ns sixsq.slipstream.client.api.authn)

(defprotocol authn
  "This protocol (interface) defines convenience functions that simplify
   authentication with the SlipStream server.

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

  (login
    [this login-params]
    [this login-params options]
    "Uses the given `login-params` to log into the SlipStream server. The
     `login-params` must be a map containing an `:href` element giving the id
     of the sessionTemplate resource and any other attributes required for the
     login method. The `login-params` for logging in with a username and
     password would be similar to the following:

     ```
     {:href \"session-template/internal\"
      :username \"user\"
      :password \"password\"}
     ```

     The function returns a map with the response. Successful responses will
     contain a status code of 201, the resource-id of the created session, and
     a message. Errors will return a similar map with an error code and
     descriptive message.

     Authenticating using other methods requires referencing different
     Session Template resources.  For example, `login-params` like:

     ```
     {:href \"session-template/api-key\"
      :key \"credential/uuid\"
      :secret \"secret.value\"}
     ```

     could be used to authenticate with an API key/secret.  Note that the
     template names will depend on the configuration of the SlipStream
     server.")

  (logout
    [this]
    [this options]
    "Performs a logout of the client by deleting the current session. On
     success, returns a map with a 200 status code and a message. The function
     returns nil if the user is not currently authenticated.")

  (authenticated?
    [this]
    [this options]
    "Returns true if the client has an active, valid session; returns false
     otherwise (even for errors)."))
