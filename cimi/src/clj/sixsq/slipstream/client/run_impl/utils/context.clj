(ns ^{:no-doc true} sixsq.slipstream.client.run-impl.utils.context
  (:require
    [clojure-ini.core :refer [read-ini]]
    [clojure.java.io :as io]
    [clojure.tools.logging :as log]
    [sixsq.slipstream.client.run-impl.utils.path :as cu]))

;;
;; Location defaults.

(def ^:private ^:const ss-client-home
  (->> ["opt" "slipstream" "client"]
       cu/path-join
       cu/force-absolute))

;;
;; Configuration.

(def ^:private ^:const context-fn "slipstream.context")

(def ^:private ^:const context-file-locs
  [(System/getProperty "user.dir")
   (System/getProperty "user.home")
   (cu/path-join [ss-client-home "bin"])
   (cu/path-join [ss-client-home "sbin"])
   (System/getProperty "java.io.tmpdir")])

(def ^:private resource-context
  (if-let [f (io/resource context-fn)] (.getPath f)))

(defn- context-file-paths
  []
  (->> context-file-locs
       (map (partial cu/path-append context-fn))
       (concat [resource-context])
       (remove nil?)))

(defn file-exists?
  [file-name]
  (.exists (io/as-file file-name)))

(defn find-file []
  (->> (context-file-paths)
       (filter file-exists?)
       first))

(defn get-context
  []
  (if-let [conf (find-file)]
    (do
      (log/debug "Found configuration file: " conf)
      (read-ini conf
                :keywordize? true
                :comment-char \#))
    (throw (IllegalStateException. "Failed to find configuration file."))))


