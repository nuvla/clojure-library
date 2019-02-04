(ns ^{:no-doc true} sixsq.slipstream.client.impl.utils.utils
  (:require [clojure.string :as s]
            [clojure.walk :as walk]))

(defn in?
  [x xs]
  (boolean ((set xs) x)))

(defn url-join
  "Trivial joiner of a sequence on '/'.
  Not meant to be following RFC 3986.
  "
  [& [parts]]
  (s/join "/" parts))

(defn- remove-blanks
  [m]
  (->> (walk/stringify-keys m)
       (remove #(s/blank? (first %)))))

(defn to-body-params
  [query-map & [on]]
  (->> (remove-blanks query-map)
       (map #(s/join "=" %))
       (s/join (or on "&"))))

(defn split
  [s on]
  (s/split s on))

(defn keywordize-keys
  [d]
  (walk/keywordize-keys d))

