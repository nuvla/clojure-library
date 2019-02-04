(ns sixsq.slipstream.client.run
  "
  ## Purpose

  The `run` namespace contains functions for interacting with SlipStream runs.  This
  is a higher level interface that is only available from Clojure. The functions in
  this namespace may change without notice.

  It allows users to

   * Scale the components of the running application up and down,
   * Query the state of the run,
   * Query and set parameters of the application components and their instances,
   * Query parameters from global namespace (`ss:`), and
   * Terminate the run.

  Timeouts and intervals are in seconds.

  ## SlipStream Run Terminology

  There are three types of parameters on a run

   * Global parameter - `ss:tags`
   * Application component parameter - `webapp:ids`
   * Application component instance parameter - `webapp.1:hostname`

  where

   * `ss` is the global namespace of the run,
   * `webapp` is the name of the application component, which is referred to as `comp`
     in the API,
   * `webapp.1` is the name of the instance `1` of the application component `webapp`.


  ## Usage and examples

  The namespace can be bootstrapped either from `slipstream.context` file
  (in .ini format) or by explicitly providing the configuration to `contextualize!`.

  In the case of `slipstream.context`, the following structure and parameters
  are assumed

      [contextualization]

      # SlipStream endpoint.
      serviceurl = https://nuv.la

      # SlipStream user credentials
      username = foo
      password = bar

      # preferred over username and password
      cookie = key=val; Path:/

      # run UUID
      diid = 123

  This file is usually available on SlipStream managed VMs and is used by
  orchestrator and node executors.

  First call to any of the API functions searches and loads the
  contextualisation file.  All successive calls use the locally bound configuration.

  Explicitly calling `contextualize!` w/o parameters reload the contextualization
  and updates the configuration of the namespace.

  `contextualize!` can be called with a context map

      {:serviceurl \"https://nuv.la\"

       ;; run UUID
       :diid \"123\"

       :cookie \"key=val; Path:/\"

       :username \"foo\"
       :password \"bar\"}

  where either `:cookie` or `:username` and `:password` are mandatory.

  ### Examples

  Implicit bootstrap -- `slipstream.context` with the correct parameters
  should be in the search path.

      (require '[sixsq.slipstream.client.api.run :as r])

      (r/get-run-info)

  Manually define configuration and contextualize the namespace.

      (require '[sixsq.slipstream.client.api.run :as r])

      (def conf {:serviceurl \"https://nuv.la\"
                 :diid \"123-456\"
                 :username \"foo\"
                 :password \"bar\"})
      (rw/contextualize! conf)

      (r/get-run-info)"
  (:require
    [sixsq.slipstream.client.api.deprecated-authn :as a]
    [sixsq.slipstream.client.run-impl.lib.run :as r]
    [sixsq.slipstream.client.run-impl.utils.context :as c]))

(def ^:dynamic *context* {})

(defn contextualize!
  ([]
   (alter-var-root #'*context* (constantly (:contextualization (c/get-context))))
   (a/set-context! *context*))
  ([context]
   (alter-var-root #'*context* (constantly context))
   (a/set-context! context)))

(defn run-uuid [] (or (:diid *context*)
                      (do (contextualize!)
                          (:diid *context*))))


;;
;; Public library API.


;; Getters and setters of component, component instance and global parameters.
(defn get-param
  "Get parameter 'param' of application component instance 'comp.id' as 'comp.id:param'.
  When 'id' is nil, gets parameter of the application component as 'comp:param'."
  [comp id param]
  (r/get-param (run-uuid) comp id param))

(defn set-param
  "Set parameter 'param' to 'value' on application component instance 'comp.id'."
  [comp id param value]
  (r/set-param (run-uuid) comp id param value))

(defn set-params
  "Given a map of parameters 'params', set them on application component
  instance 'comp.id'."
  [comp id params]
  (r/set-params (run-uuid) comp id params))

(defn get-scale-state
  "Get scale state of the component instance."
  [comp id]
  (r/get-scale-state (run-uuid) comp id))

(defn get-state
  "Get state of the run."
  []
  (r/get-state (run-uuid)))

(defn get-abort
  "Get abort message."
  []
  (r/get-abort (run-uuid)))

(defn get-multiplicity
  "Get multiplicity of application component 'comp'."
  [comp]
  (r/get-multiplicity (run-uuid) comp))

(defn get-comp-ids
  "Get list of instance IDs of application component 'comp'."
  [comp]
  (r/get-comp-ids (run-uuid) comp))


;; Predicates.
(defn aborted?
  "Check if run is in 'Aborted' state."
  []
  (r/aborted? (run-uuid)))

(defn scalable?
  "Check if run is scalable."
  []
  (r/scalable? (run-uuid)))

(defn can-scale?
  "Check if it's possible to scale the run."
  []
  ; TODO: Use single call to get run representation and query it locally.
  ;       At the moment it's xml, which is not that comfortable to parse.
  (r/can-scale? (run-uuid)))

(defn get-run-info
  []
  (r/get-run-info (run-uuid)))

(defn action-success?
  "Given the 'result' returned by an action, check if it was successfull."
  [result]
  (r/action-success? result))


;; Actions on the run.
(def wait-timeout-default 600)

(defn cancel-abort
  "Cancel abort on the run."
  []
  (r/cancel-abort (run-uuid)))

(defn terminate
  "Terminate the run."
  []
  (r/terminate (run-uuid)))

(defn scale-up
  "Scale up application component 'comp' by 'n' instances. Allow to set parameters
  from 'params' map on the new component instances. Returns list of added component
  instance names qualified with IDs."
  ([comp n]
   (r/scale-up (run-uuid) comp n))
  ([comp n params]
   (r/scale-up (run-uuid) comp n params)))

(defn scale-down
  "Scale down application component 'comp' by terminating instances defined by
  'ids' vector."
  [comp ids]
  (r/scale-down (run-uuid) comp ids))

(defn wait-provisioning
  "Waits for Provisioning state on the run. Returns true on success."
  ([]
   (r/wait-provisioning (run-uuid)))
  ([timeout]
   (r/wait-provisioning (run-uuid) timeout)))

(defn wait-ready
  "Waits for Ready state on the run. Returns true on success."
  ([]
   (r/wait-ready (run-uuid)))
  ([timeout]
   (r/wait-ready (run-uuid) timeout)))


;; Composite actions.
(defn action-scale-up
  "Scale application component 'comp' by 'n' instances up.  Wait for the
  completion of the action. Optionally provide map of parameters as 'params'."
  [comp n & [& {:keys [params timeout]
                :or   {params {} timeout wait-timeout-default}}]]
  (r/action-scale-up (run-uuid) comp n :params params :timeout timeout))

(defn action-scale-down-by
  "Scale down application component 'comp' by 'n' instances. Wait for
  the completion of the action."
  [comp n & [& {:keys [timeout]
                :or   {timeout wait-timeout-default}}]]
  (r/action-scale-down-by (run-uuid) comp n :timeout timeout))

(defn action-scale-down-at
  "Scale down application component 'comp' by terminating the component
  instances identified by IDs in 'ids'. Wait for the completion of the action."
  [comp ids & [& {:keys [timeout]
                  :or   {timeout wait-timeout-default}}]]
  (r/action-scale-down-at (run-uuid) comp ids :timeout timeout))

