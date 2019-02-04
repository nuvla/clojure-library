#!/usr/bin/env boot
"
## Purpose

This test accepts user parameters, starts a scalable run and scales it
up, down and diagonally.  This validates the library and the behaviour
of the SlipStream scalable deployment.

## Prerequisites.

1. Define a single component SlipStream application.  By default, the
test assumes that the name of the application component is
'testvm'. The application will be started with an explicit
multiplicity of 1 for the scaled application component.

Check the 'usage' var below for the CLI parameters.

2. Run the test with

  # ./test/live/scalable-run.clj <params>

You must run it from the `jar` subdirectory in order to find the
correct files on the classpath.

3. If test fails, you need to terminate the deployment manually.  The
deployment URL can be found at the very beginning of the test
output.  Example:

```
    :::
    ::: Start scalable run.
    :::
    ::: Run:
    {:url \"https://nuv.la/run/b61d28b9-acec-4f79-8909-f0e603b3fa64\",
     :state \"Initializing\",
     :scalable true,
     :can-scale false,
     :aborted false,
     :abort-msg \"\"}
    :::
```
"

(def usage
  "Usage: ./test/live/scalable-run.clj user pass app-uri [endpoint] [component-name] [new-VM-size]
  user           - name of the user (mandatory)
  pass           - password of the user (mandatory)
  app-uri        - uri of the application to provision (mandatory)
  endpoint       - SlipStream endpoint. Default: https://nuv.la
  component-name - name of the application component. Default: testvm
  new-VM-size    - VM size for the diagonal scaling test. Default: Tiny
  ")

;;
;; Boot related scaffolding.
(def artifact-version "3.37-SNAPSHOT")
(def repo-type (if (re-find #"SNAPSHOT" artifact-version) "snapshots" "releases"))
(def edition "community")
  (def nexus-url "https://nexus.sixsq.com/content/repositories/")

(set-env!
  :source-paths #{"src/clj" "src/cljc"}
  ;:resource-paths #{"resources"}

  :repositories #(reduce conj %
                         [["boot-releases" {:url (str nexus-url "releases-boot")}]
                          ["sixsq" {:url (str nexus-url repo-type "-" edition)}]])

  :dependencies
  '[[sixsq/boot-deputil "0.1.0" :scope "test"]])

(require
  '[sixsq.boot-deputil :refer [set-deps!]])

(boot (comp
        (checkout :dependencies [['sixsq/default-deps artifact-version]])
        (set-deps!)))
;; Boot end.


;;
;; Dynamic vars.
(def ^:dynamic *username* nil)
(def ^:dynamic *password* nil)
(def ^:dynamic *app-uri* nil)
(def ^:dynamic *serviceurl* "https://nuv.la")

; Name of the deployed component to be used for scaling.
(def ^:dynamic *comp-name* "testvm")

; Cloud releated instance type. Used below in diagonal scale up action.
(def ^:dynamic *test-instance-type* "Tiny")


;;
;; Imports.
(require '[sixsq.slipstream.client.api.deprecated-authn :as a])
(require '[sixsq.slipstream.client.run-impl.lib.app :as p])

(require '[sixsq.slipstream.client.run :as r] :reload)
(use '[clojure.pprint :only [pprint]])


;;
;; Helper functions.
(defn print-run
  []
  (pprint (r/get-run-info)))

(defn action [& msg]
  (apply println ":::\n:::" msg))

(defn step [& msg]
  (apply println "   -" msg))

(defn error [& msg]
  (apply println "ERROR:" msg)
  (print-run)
  ((System/exit 0)))

(defn wait-ready-or-error
  []
  (step "Waiting for Ready state.")
  (if-not (true? (r/wait-ready))
    (error "Failed waiting for the run to enter Ready state.")))

(defn check-scalable
  []
  (step "Check if run is scalable.")
  (if-not (true? (r/scalable?))
    (error "Run is not scalable.")))

(defn check-multiplicity
  [exp]
  (if-not (= exp (r/get-multiplicity *comp-name*))
    (error (format "Multiplicity should be %s." exp))))

(defn check-instance-ids
  [exp]
  (let [exp-str (map str exp)]
    (if-not (= exp-str (r/get-comp-ids *comp-name*))
      (error (format "Instance IDs should be %s." exp-str)))))

(defn check-can-scale
  []
  (if-not (true? (r/can-scale?))
    (error "Should be able to scale at this stage.")))

(defn check-cannot-scale
  []
  (if-not (false? (r/can-scale?))
    (error "Should NOT be able to scale at this stage.")))

(defn inst-names-range
  [start stop]
  (vec (map #(str *comp-name* "." %) (range start stop))))

(defn prn-inst-scale-states
  [ids]
  (println (map
             #(list (str *comp-name* "." %) (r/get-scale-state *comp-name* %))
             ids)))

(defn run-uuid-from-run-url
  [run-url]
  (-> run-url
      clojure.string/trim
      (clojure.string/split #"/")
      last
      clojure.string/trim))


;; - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
(defn run []
  (step "Live test of the SlipStream clojure API library via scaling SlipStream run.")
  (step (format "User: '%s'" *username*))
  (step (format "Endpoint: '%s'" *serviceurl*))
  (step (format "Application uri: '%s'" *app-uri*))
  (step (format "Application component name to scale: '%s'" *comp-name*))
  (step (format "VM instance type for diagonal scaling: '%s'" *test-instance-type*))

  ;;
  ;; Login to the SlipStream portal.
  ;;
  (action "Login to SlipStream.")
  (a/login! *username* *password* (a/to-login-url *serviceurl*))

  ;;
  ;; Deploy the application in a scalable mode.
  ;;
  (action "Start scalable run.")
  (def run-uuid
    (run-uuid-from-run-url (p/deploy *app-uri* {:scalable true
                                                (str *comp-name* ":multiplicity") 1})))
  ; Re-contextualize the run namespace with the deployment uuid.
  (r/contextualize! (assoc a/*context* :diid run-uuid))

  (action "Run:")
  (print-run)

  ;;
  ;; Actions and assertions.
  ;;
  (action "Starting tests.")
  (wait-ready-or-error)
  (check-scalable)
  (check-multiplicity 1)
  (check-instance-ids '(1))
  (check-can-scale)
  (prn-inst-scale-states '(1))

  (action "Artificially abort the run and then recover from the abort.")
  (try
    (r/get-param "foo" 0 "bar")
    (catch clojure.lang.ExceptionInfo e (let [status (:status (ex-data e))]
                                          (if-not (= 404 status) (error "Unexpected HTTP error status:" status))))
    (catch Exception e (error "Unexpected error:" (ex-data e))))
  (check-cannot-scale)
  (if-not (true? (r/aborted?))
    (error "Run should be aborted at this stage."))
  (if-not (= "Unknown key foo.0:bar" (r/get-abort))
    (error "Unexpected abort message."))
  ; FIXME: run/cancel-abort should return run/action-result map
  (if-not (= 204 (:status (r/cancel-abort)))
    (error "Failed cancelling the abort flag."))
  (check-can-scale)


  (action "Scale up. Manual wait.")
  (let [exp-inst-names (inst-names-range 2 4)
        inst-names     (r/scale-up *comp-name* 2)]
    (if-not (= exp-inst-names inst-names)
      (error "Expected to start" exp-inst-names ", but started" inst-names)))
  (wait-ready-or-error)
  (check-multiplicity 3)
  (check-instance-ids '(1 2 3))
  (check-can-scale)


  (action "Scale down by IDs. Manual wait.")
  (if-not (clojure.string/blank? (r/scale-down *comp-name* '(3 1)))
    (error "Scale down should have returned empty string."))
  (wait-ready-or-error)
  (check-multiplicity 1)
  (check-instance-ids '(2))
  (check-can-scale)


  (action "Diagonal scale up action (with internal wait). Providing VM size RTPs.")
  (def cloudservice (r/get-param *comp-name* 1 "cloudservice"))
  (def key-instance-type (str cloudservice ".instance.type"))
  (let [res (r/action-scale-up *comp-name* 2
                               :params {key-instance-type *test-instance-type*}
                               :timeout 1200)]
    (if-not (and (r/action-success? res) (= (inst-names-range 4 6) (:reason res)))
      (error "Diagonal scale up failed:" res)))
  (check-multiplicity 3)
  (check-instance-ids '(2 4 5))
  (step "'component id' => 'instance size'")
  ; TODO: add asserts for IDs 4 and 5
  (doseq [id (r/get-comp-ids *comp-name*)]
    (step (format "%s => %s"
                  id (r/get-param *comp-name* id key-instance-type))))
  (check-can-scale)


  (def inst-down '(2 4))
  (action "Scale down action (with internal wait). Remove instances by ids:" inst-down)
  (let [res (r/action-scale-down-at *comp-name* inst-down :timeout 1200)]
    (if-not (r/action-success? res)
      (error "Failed scaling down:" res)))
  (check-multiplicity 1)
  (check-instance-ids '(5))
  (check-can-scale)


  (action "Scale down action (with internal wait). Remove a number of instances.")
  (let [res (r/action-scale-down-by *comp-name* 1 :timeout 1200)]
    (if-not (r/action-success? res)
      (error "Failed scaling down:" res)))
  (check-multiplicity 0)
  (check-instance-ids '())
  (check-can-scale)


  (action "Terminating run.")
  ; FIXME: run/terminate should return run/action-result map
  (let [res (r/terminate)]
    (if-not (= 204 (:status res))
      (error "Failed to properly terminate the run:" res)))


  (action "Validation.")
  (let [res (r/get-state)]
    (if-not (= "Done" res)
      (error "Expected the run in Done state. Found:" res)))
  (check-cannot-scale)

  (action "Test finished successfully."))

;;
(defn exit-usage []
  (println usage)
  (System/exit 1))

(defn -main [& args]
  (if (< (count args) 3)
    (exit-usage)
    (do
      (alter-var-root #'*username* (fn [_] (nth args 0)))
      (alter-var-root #'*password* (fn [_] (nth args 1)))
      (alter-var-root #'*app-uri* (fn [_] (nth args 2)))))
  (if (> (count args) 3)
    (alter-var-root #'*serviceurl* (fn [_] (nth args 3))))
  (if (> (count args) 4)
    (alter-var-root #'*comp-name* (fn [_] (nth args 4))))
  (if (> (count args) 5)
    (alter-var-root #'*test-instance-type* (fn [_] (nth args 5))))
  (run)
  (System/exit 0))
