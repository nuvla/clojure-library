(ns ^{:no-doc true} sixsq.nuvla.client.impl.utils.json
  "Utilities for handling JSON data."
  (:require
    #?(:clj
       [clojure.data.json :as json])
    [sixsq.nuvla.client.impl.utils.error :as e]))


(defn kw->str
  "Converts a keyword to the equivalent string without the leading colon and
   **preserving** any namespace."
  [kw]
  (subs (str kw) 1))


(defn key-fn
  "Converts a keyword to the equivalent string without the leading colon and
   **preserving** any namespace."
  [k]
  (if (keyword? k)
    (kw->str k)
    (str k)))


(defn str->json [s]
  #?(:clj  (try
             (json/read-str s :key-fn keyword)
             (catch Exception _
               s))
     :cljs (try
             (js->clj (js/JSON.parse s) :keywordize-keys true)
             (catch :default _
               s))))


(defn edn->json [edn]
  #?(:clj  (json/write-str edn :key-fn key-fn)
     :cljs (js/JSON.stringify (clj->js edn :keyword-fn kw->str))))


(defn json->edn [json]
  (cond
    (nil? json) {}
    (e/error? json) json
    :else (str->json json)))


(defn body-as-json
  "transducer that extracts the body of a response and parses
   the result as JSON"
  []
  (comp
    (map e/throw-if-error)
    (map :body)
    (map json->edn)))
