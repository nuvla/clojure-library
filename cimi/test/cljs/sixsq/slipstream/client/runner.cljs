(ns sixsq.slipstream.client.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [sixsq.slipstream.client.cimi-async-lifecycle-test]
            [sixsq.slipstream.client.impl.utils.cimi-test]
            [sixsq.slipstream.client.impl.utils.common-test]
            [sixsq.slipstream.client.impl.utils.error-test]
            [sixsq.slipstream.client.impl.utils.http-utils-test]
            [sixsq.slipstream.client.impl.utils.json-test]
            [sixsq.slipstream.client.impl.utils.run-params-test]
            [sixsq.slipstream.client.impl.utils.utils-test]
            [sixsq.slipstream.client.impl.utils.wait-test]
            [sixsq.slipstream.client.run-impl.run-test]
            ))

(doo-tests
  'sixsq.slipstream.client.impl.utils.utils-test
  'sixsq.slipstream.client.impl.utils.error-test
  'sixsq.slipstream.client.impl.utils.wait-test
  'sixsq.slipstream.client.impl.utils.http-utils-test
  'sixsq.slipstream.client.impl.utils.json-test
  'sixsq.slipstream.client.cimi-async-lifecycle-test
  'sixsq.slipstream.client.impl.utils.run-params-test
  'sixsq.slipstream.client.impl.utils.cimi-test
  'sixsq.slipstream.client.run-impl.run-test
  'sixsq.slipstream.client.impl.utils.common-test
  )
