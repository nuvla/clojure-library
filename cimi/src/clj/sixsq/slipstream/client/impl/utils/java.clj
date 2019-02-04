(ns ^{:no-doc true} sixsq.slipstream.client.impl.utils.java
  "Utilities convert between java and clojure data structures. The algorithm
   was written by Baishampayan Ghose and can be found here:
   http://grokbase.com/t/gg/clojure/11afb4wmb3/recursively-convert-java-map-to-clojure-map."
  (:require [clojure.walk :as walk]))

(defprotocol ConvertToClojure
  (->clj [o]))

(defn- assoc-keywordized-entry
  [m [^String k v]]
  (assoc m (keyword k) (->clj v)))

(extend-protocol ConvertToClojure
  java.util.Map
  (->clj [o] (reduce assoc-keywordized-entry {} (.entrySet o)))

  java.util.List
  (->clj [o] (vec (map ->clj o)))

  java.util.Set
  (->clj [o] (set (map ->clj o)))

  java.lang.Object
  (->clj [o] o)

  nil
  (->clj [_] nil))

(defn to-clojure
  "Converts from mutable java collections to immutable, persistent clojure
   data structures. Keywordizes keys in the process."
  [m]
  (->clj m))

(defn to-java
  "Changes keywordized keys to strings to make the data structure compatible
   with java. Note that the result is still an immutable, persistent clojure
   data structure."
  [m]
  (walk/stringify-keys m))
