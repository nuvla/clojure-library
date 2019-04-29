(ns sixsq.nuvla.client.runner
  (:require
    [doo.runner :refer-macros [doo-tests]]
    [sixsq.nuvla.client.async-lifecycle-test]
    [sixsq.nuvla.client.impl.utils.cimi-test]
    [sixsq.nuvla.client.impl.utils.common-test]
    [sixsq.nuvla.client.impl.utils.error-test]
    [sixsq.nuvla.client.impl.utils.http-utils-test]
    [sixsq.nuvla.client.impl.utils.json-test]))

(doo-tests
  'sixsq.nuvla.client.async-lifecycle-test
  'sixsq.nuvla.client.impl.utils.cimi-test
  'sixsq.nuvla.client.impl.utils.common-test
  'sixsq.nuvla.client.impl.utils.error-test
  'sixsq.nuvla.client.impl.utils.http-utils-test
  'sixsq.nuvla.client.impl.utils.json-test)
