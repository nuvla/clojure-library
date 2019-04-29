(ns ^{:no-doc true} sixsq.nuvla.client.impl.utils.common
  "Provides utilities that support the SCRUD actions for resources.
   Although these functions are public, they are not part of the public
   API and may change without notice.")

;; The the :follow-redirects setting asks that redirects are handled
;; directly by the client rather than by the underlying HTTP library.
;; However those HTTP libraries, particularly XHR from browsers, will
;; still handle redirects automatically. Be careful of this behavior.
(def ^:dynamic *std-opts* {:type             :json
                           :accept           :json
                           :follow-redirects false})


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
