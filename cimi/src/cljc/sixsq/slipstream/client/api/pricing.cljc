(ns sixsq.slipstream.client.api.pricing)

(defprotocol pricing
  "Functions to interact with the placement and ranking services."

  (place-and-rank
    [this module-uri connectors]
    [this module-uri connectors options]
    "Call the '/ui/placement' and '/filter-rank' resources to get the
     deployment information."))
