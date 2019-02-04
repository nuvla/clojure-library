(ns sixsq.slipstream.client.api.modules)

(defprotocol modules
  "Provides functions to retrieve SlipStream modules.

  This protocol targets the legacy SlipStream interface and provides
  limited functionality. The module resources are being migrated to
  the CIMI protocol and this protocol will disappear once that migration
  is complete.

  Note that the return types will depend on the concrete implementation. For
  example, an asynchronous implementation will return channels from all of
  the functions."

  (get-module
    [this url-or-id]
    [this url-or-id options]
    "Reads the module identified by the URL or id and returns a data structure
     containing a full description of the module.")

  (get-module-children
    [this url-or-id]
    [this url-or-id options]
    "Reads the module identified by the URL or id and returns a list of the
     child identifiers. If the argument is nil, then the list of root modules
     is returned. If the module has no children, an empty list is returned. If
     the module is not a project (i.e. can't have children), then nil is
     returned."))
