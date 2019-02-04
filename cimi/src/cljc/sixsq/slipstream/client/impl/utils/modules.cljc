(ns ^{:no-doc true} sixsq.slipstream.client.impl.utils.modules
  "Utilities specific to working with modules.")


(defn select-child-fields [child]
  (select-keys child #{:name :description :category :version}))


(defn process-children [module path-vector]
  (when-let [children (get-in module path-vector)]
    (let [children (if (map? children) [children] children)] ;; may be single item!
      (map select-child-fields children))))


(defn extract-children [module]
  (process-children module [:projectModule :children :item]))


(defn extract-root-children [root]
  (process-children root [:list :item]))


