(ns sixsq.slipstream.client.impl.utils.run-params
  (:require
    [clojure.string :as str]
    [sixsq.slipstream.client.impl.utils.error :as e]))

(def param-refqname "refqname")
(def param-scalable "mutable")
(def param-keep-running "keep-running")
(def param-tags "tags")
(def param-type "type")

(def params-reserved #{param-tags
                       param-keep-running
                       param-type})


(defn node-parameter [k v]
  (let [[comp param] (str/split k #":")]
    (if-not (or (str/blank? comp) (str/blank? param))
      {(str "parameter--node--" comp "--" param) (str v)})))


(defn process-kv
  "process a key/value pair to be included in run parameters"
  [k v]
  (let [k (str/trim (name k))]
    (cond
      (str/blank? k) nil
      (= "scalable" k) {param-scalable (str v)}
      (params-reserved k) {k (name v)}
      (str/includes? k ":") (node-parameter k v)
      :else {(str "parameter--" k) (str v)})))


(defn process-params-fn
  "reduction function that accumulates map entries with the processed keys and
   values"
  [m k v]
  (merge m (process-kv k v)))


(defn parse-params
  [params]
  (reduce-kv process-params-fn {} params))


(defn assoc-module-uri
  [params uri]
  (assoc params param-refqname (str "module/" uri)))


(defn extract-location
  "transducer that extracts the value of the location header from the response"
  []
  (comp
    (map e/throw-if-error)
    (map :headers)
    (map :location)))
