(ns sixsq.slipstream.client.api.runs)

(defprotocol runs
  "Functions to get and search for runs.

   This protocol targets the legacy SlipStream interface and provides limited
   functionality. The deployment resources are being migrated to the CIMI
   protocol and this protocol will disappear once that migration is complete.

   Note that the return types will depend on the concrete implementation. For
   example, an asynchronous implementation will return channels from all of the
   functions."

  (get-run
    [this url-or-id]
    [this url-or-id options]
    "Reads the run identified by the URL or resource id. Returns the run as
     EDN data.")

  (start-run
    [this uri]
    [this uri options]
    "Starts the application or component identified by its URI. Returns the
     run identifier.

     Deployment and component parameters are expected in `options` map.

     The following reserved deployment parameters are recognized:

       - `{:scalable true|false}` to start a scalable deployment;
       - `{:keep-running :never|:always|:on-error|:on-success}` to change the
           post-deployment state;
       - `{:tags \"comma-separated-list\"}` to label the deployment

     Application component parameters should be provided in the following form:

       - `{\"comp-name:param-name\" val}`

     E.g., to deploy component on a specific cloud use:

       - `{\"comp-name:cloudservice\" \"cloud-connector-name\"}`

     To start a component, add the following to the options:

       - `{:type \"Run\"}`")

  (terminate-run
    [this url-or-id]
    [this url-or-id options]
    "Terminates the run identified by the URL or resource id.")

  (search-runs
    [this]
    [this options]
    "Search for runs of the given type, returning a list of the matching runs.
     Supported options are :cloud, :activeOnly, :offset, and :limit. The
     returned document is in EDN format."))
