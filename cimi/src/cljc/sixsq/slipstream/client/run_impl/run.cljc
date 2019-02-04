(ns ^{:no-doc true} sixsq.slipstream.client.run-impl.run
  (:refer-clojure :exclude [get])
  (:require
    [clojure.string :as s]
    [sixsq.slipstream.client.impl.utils.utils :as u]))

(def ^:const global-ns "ss")

(def ^:const comp-prop-sep ":")
(def ^:const comp-mult-sep ".")

(def ^:const state-param (str global-ns comp-prop-sep "state"))
(def ^:const abort-param (str global-ns comp-prop-sep "abort"))

(def ^:const non-scalable-final-states ["Finalizing" "Done"])
(def ^:const scalable-states ["Ready"])


(defn run-uri
  [run-uuid]
  (u/url-join ["run" run-uuid]))

(defn run-url
  [service-url run-uuid]
  (u/url-join [service-url (run-uri run-uuid)]))

(defn run-state-uri
  [run-uuid]
  (u/url-join [(run-uri run-uuid) state-param]))

(defn run-state-url
  [service-url run-uuid]
  (u/url-join [service-url (run-state-uri run-uuid)]))

(defn run-abort-uri
  [run-uuid]
  (u/url-join [(run-uri run-uuid) abort-param]))

(defn run-abort-url
  [service-url run-uuid]
  (u/url-join [service-url (run-abort-uri run-uuid)]))


(defn to-param
  "Retruns parameter as 'comp.id:param' or 'comp:param' if 'id' is nil."
  [comp id param]
  (if id
    (str comp comp-mult-sep id comp-prop-sep param)
    (str comp comp-prop-sep param)))

(defn to-param-url
  "Returns parameter full URL as 'comp.id:param' or
  'comp:param if 'id' is nil."
  [service-url run-uuid comp id param]
  (u/url-join [(run-url service-url run-uuid) (to-param comp id param)]))

(defn to-param-uri
  "Returns parameter full URI as 'run/run-uuid/comp.id:param' or
  'run/run-uuid/comp:param if 'id' is nil."
  [run-uuid comp id param]
  (u/url-join [(run-uri run-uuid) (to-param comp id param)]))

(defn to-component-uri
  [run-uuid comp]
  (u/url-join [(run-uri run-uuid) comp]))

(defn to-component-url
  [service-url run-uuid comp]
  (u/url-join [(run-url service-url run-uuid) comp]))

(defn- extract-id
  [s]
  (->> (s/trim s)
       (re-seq #"\.(\d*$)")
       first
       last))

(defn extract-ids
  [names]
  (->> (if (string? names) (s/split names #",") names)
       (map extract-id)
       (remove nil?)))

