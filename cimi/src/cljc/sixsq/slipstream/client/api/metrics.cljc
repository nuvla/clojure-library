(ns sixsq.slipstream.client.api.metrics)

(defprotocol metrics
  "This protocol (interface) defines the function to return the service
   metrics from the server, including JVM, ring, and other metrics."

  (get-metrics
    [this options]
    "Retrieves the service metrics from the server. These are the raw
    JSON-formatted metrics produced by the Coda Hale metrics library."))
