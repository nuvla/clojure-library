(ns ^{:no-doc true} sixsq.slipstream.client.run-impl.utils.path
  (:require [clojure.string :as str])
  (:import [java.io File]))

(defn force-absolute
  [path]
  (if (.startsWith path File/separator)
    path
    (str File/separator path)))

(defn path-join
  [paths]
  (str/join File/separator paths))

(defn path-append
  [path paths]
  (str paths File/separator path))
