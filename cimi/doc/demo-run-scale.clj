"Prerequisites.

1. Define a single component SlipStream application.

2. The functions in the demo are intended to be run manually in REPL.
For example, go to jar/ directory of the project and start REPL with

    $ boot repl

Now you should be ready to proceed.
"

;;
;; Redefine the following variables.

; SlipStream URL and credentials
(def serviceurl "https://nuv.la")
(def username "change_me")
(def password "change_me")

; Application URI.  Example:
(def app "konstan/scale/scale-test-dpl")

; Name of the application component from `app` to be used for scaling.
(def comp-name "testvm")

; Cloud releated instance type. Used below in diagonal scale up action.
(def test-instance-type "Tiny")


;;
;; Imports.
(require '[sixsq.slipstream.client.api.deprecated-authn :as a])
(require '[sixsq.slipstream.client.api.lib.app :as p])
(require '[sixsq.slipstream.client.api.run :as r])

;;
;; Login to the SlipStream portal.
(a/login! username password (a/to-login-url serviceurl))

(defn run-uuid-from-run-url
  [run-url]
  (-> run-url
      clojure.string/trim
      (clojure.string/split #"/")
      last
      clojure.string/trim))

; Deploy the application in a scalable mode.
(def run-uuid (run-uuid-from-run-url (p/deploy app {:scalable true})))

; Re-contextualize the run namespace with the deployment uuid.
(r/contextualize! (assoc a/*context* :diid run-uuid))

; Wait for Ready state in case the deployment is still provisioning.
(r/wait-ready 1200)

; Queries.
(r/get-state)

(r/scalable?)

(r/get-multiplicity comp-name)
(r/get-comp-ids comp-name)

(r/can-scale?)

; Artificially abort the run and then recover from the abort.
(r/get-param "foo" 0 "bar")
(r/can-scale?)
(r/aborted?)
(r/get-abort)
(r/cancel-abort)

(r/can-scale?)

; Scale up. No wait.
(r/scale-up comp-name 1)
(r/get-multiplicity comp-name)
(r/get-comp-ids comp-name)

(r/can-scale?)
(r/wait-ready 900)

; Scale up. Manual wait.
(r/scale-up comp-name 3)
(r/wait-ready 900)
(r/get-multiplicity comp-name)
(r/get-comp-ids comp-name)

; Scale down by IDs. Manual wait.
(r/scale-down comp-name [4 1])
(r/wait-ready 900)
(r/get-multiplicity comp-name)
(r/get-comp-ids comp-name)

; Scale up action. Provide parameters. Use internal wait.
(def cloudservice (r/get-param comp-name 1 "cloudservice"))
(def key-instance-type (str cloudservice ".instance.type"))
(r/action-scale-up comp-name 2
                   :params {key-instance-type test-instance-type}
                   :timeout 1200)
(r/get-multiplicity comp-name)
(r/get-comp-ids comp-name)
(doseq [id (r/get-comp-ids comp-name)]
  (println (format "%s = %s"
                   id (r/get-param comp-name id key-instance-type))))

; Scale down action. Remove instances 2, 3 and 6.  Use internal wait.
(r/action-scale-down-at comp-name [2 3 6] :timeout 1200)
(r/get-multiplicity comp-name)
(r/get-comp-ids comp-name)

; Scale down action. Remove to instances. Use internal wait.
(r/action-scale-down-by comp-name 2 :timeout 1200)
(r/get-multiplicity comp-name)
(r/get-comp-ids comp-name)

; Terminate run.
(r/terminate)

; Validation.
(r/get-state)
(r/can-scale?)
