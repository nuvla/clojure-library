(ns ^{:no-doc true} sixsq.slipstream.client.impl.utils.common
  "Provides utilities that support the SCRUD actions for CIMI resources.
   Although these functions are public, they are not part of the public
   API and may change without notice."
  (:require
    #?(:clj
    [clojure.data.json :as json])
    [sixsq.slipstream.client.impl.utils.error :as e]))

;; The the :follow-redirects setting asks that redirects are handled
;; directly by the client rather than by the underlying HTTP library.
;; However those HTTP libraries, particularly XHR from browsers, will
;; still handle redirects automatically. Be careful of this behavior.
(def ^:dynamic *std-opts* {:type             :json
                           :accept           :json
                           :follow-redirects false})

(defn merge-std-opts!
  [m]
  #?(:clj (alter-var-root (var *std-opts*) merge m))
  #?(:cljs (set! *std-opts* (merge *std-opts* m))))

(defn assoc-token [m token]
  (if token
    (assoc m :headers {:cookie token})
    m))

(defn assoc-body [m body]
  (if body
    (assoc m :body body)
    m))

(defn req-opts
  ([]
   (req-opts nil nil))
  ([token]
   (req-opts token nil))
  ([token body]
   (-> *std-opts*
       (assoc-token token)
       (assoc-body body))))

(defn str->json [s]
  #?(:clj  (json/read-str s :key-fn keyword)
     :cljs (js->clj (js/JSON.parse s) :keywordize-keys true)))

(defn edn->json [json]
  #?(:clj  (json/write-str json)
     :cljs (js/JSON.stringify (clj->js json))))

(defn json->edn [s]
  (cond
    (nil? s) {}
    (e/error? s) s
    :else (str->json s)))

(defn ensure-url [endpoint url-or-id]
  (if (re-matches #"^((http://)|(https://).*)" url-or-id)
    url-or-id
    (str endpoint url-or-id)))

(defn ensure-url-slash [endpoint url-or-id]
  (if url-or-id
    (if (re-matches #"^((http://)|(https://).*)" url-or-id)
      url-or-id
      (str endpoint "/" url-or-id))
    endpoint))

(defn body-as-string
  "transducer that extracts the body of a response and returns
   the result as a string"
  []
  (comp
    (map e/throw-if-error)
    (map :body)))
