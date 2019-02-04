(ns ^{:no-doc true} sixsq.slipstream.client.impl.utils.cimi
  "Provides utilities that support the SCRUD actions for CIMI resources.
   Although these functions are public, they are not part of the public
   API and may change without notice."
  (:refer-clojure :exclude [read])
  (:require
    [clojure.set :as set]
    [clojure.string :as str]
    [sixsq.slipstream.client.impl.utils.error :as e]
    [sixsq.slipstream.client.impl.utils.json :as json]))

(def ^:const cimi-params #{:$first :$last :$filter :$select :$expand :$orderby :$aggregation})

(defn select-cimi-params
  "Strips keys from the provided map except for CIMI request parameters and
   returns that map. Returns nil if something other than a map is provided."
  [m]
  (when (map? m)
    (select-keys m cimi-params)))

(defn remove-cimi-params
  "Strips the CIMI request parameters from the provided map and returns the
   updated map. Returns nil if something other than a map is provided."
  [m]
  (when (map? m)
    (select-keys m (set/difference (set (keys m)) cimi-params))))

(defn remove-req-params
  "Strips the insecure?, sse? and :user-token options from the provided map."
  [m]
  (when (map? m)
    (dissoc m :insecure? :sse? :user-token)))

(defn update-state
  "If the token is not nil, then updates the value of the :token key inside
   the provided state atom. If the token is nil, then the state atom is not
   updated. Returns the new value of the atom or nil if no change was made."
  [state k v]
  (when (and state k v)
    (swap! state merge {k v})))

(defn error-tuple
  "Produces a response tuple containing the exception/error and a nil cookie
   value. Used to provide a uniform response from channels, even when errors
   occur. If the exception/error contains data, it is just passed on; if not,
   then the exception is wrapped in a ex-info exception and given a 500 status."
  [error]
  (if (ex-data error)
    [error nil]
    (let [msg (str "unexpected error: " (str error))
          data {:status 500, :message msg}]
      [(ex-info msg data) nil])))

(defn response-tuple
  "This extracts the HTTP response body (rendered as keywordized EDN) and the
   value of the set-cookie header and returns a tuple with the two values in
   that order."
  [{:keys [body] {:keys [set-cookie]} :headers :as response}]
  [(json/json->edn body) set-cookie])

(defn response-xduce
  "Transducer that extracts the HTTP response body and any set-cookie header
   value, returning a tuple of those values. If an error occurs, the error will
   be returned as the first element of the tuple."
  []
  (comp
    (map e/throw-if-error)
    (map response-tuple)))

(defn event-transducer
  "Transducer that extracts the data from an SSE message and converts it into
   an EDN document."
  []
  (comp
    (map e/throw-if-error)
    (map :data)
    (map json/json->edn)))

(defn unauthorized
  "Returns a tuple containing an exception that has a 403 unauthorized code
   and a reference to the resource. The second element of the tuple is nil."
  [& [resource-id]]
  (let [msg "unauthorized"
        data (merge {:status 403, :message msg}
                    (when resource-id {:resource-id resource-id}))
        e (ex-info msg data)]
    [e nil]))

(defn unknown-operation
  "Returns a tuple containing an exception that has a 400 client error
   and a reference to the resource. The second element of the tuple is nil."
  [& [resource-id]]
  (let [msg "unknown operation"
        data (merge {:status 400, :message msg}
                    (when resource-id {:resource-id resource-id}))
        e (ex-info msg data)]
    [e nil]))

(defn get-collection-url
  "Extracts the absolute URL for a the named collection from the cloud entry
   point. The collection name can be provided either as a string or a keyword.
   The capitalization of the collection name is significant; normally the value
   is camel-cased and has a trailing 's'. Returns nil if the collection does
   not exist."
  [{:keys [baseURI] :as cep} collection-name]
  (when (and baseURI collection-name)
    (let [collection (keyword collection-name)]
      (when-let [href (-> cep collection :href)]
        (str baseURI href)))))

(defn verify-collection-url
  "Verifies that the value of `collection-url` is the URL for one of the
   collections defined in the cloud entry point. If it is, then the URL is
   returned; if not, then nil is returned."
  [{:keys [baseURI] :as cep} collection-url]
  (when (and baseURI collection-url)
    (let [collection-urls-set (->> cep
                                   vals
                                   (filter map?)
                                   (map :href)
                                   (remove nil?)
                                   (map #(str baseURI %))
                                   set)]
      (collection-urls-set collection-url))))

(defn extract-op-url
  "Transducer that extracts the operation URL for the given operation. The
   return value is a possibly empty list."
  [op baseURI]
  (comp
    (map e/throw-if-error)
    (map :body)
    (map json/json->edn)
    (map :operations)
    cat
    (map (juxt :rel :href))
    (filter (fn [[k _]] (= op k)))
    (map (fn [[_ v]] (str baseURI v)))))

(defn legacy-endpoint
  "Heuristic transformation of cloud entry point URL into a legacy URL."
  [cep-url href]
  (str/join "/"
            (-> cep-url
                (str/split #"/")
                pop
                pop
                (conj href))))
