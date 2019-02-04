# SlipStream Clojure Client API

SlipStream Clojure client library to query [SlipStream server API][ss-api].

## Usage

### Query SlipStream Run

Library looks for the configuration file named `slipstream.context` in the following places
and order

```
1. in the resources directory defined for the JVM
2. current directory
3. in user home directory
4. /opt/slipstream/client/bin/
5. /opt/slipstream/client/sbin/
6. system temporary directory

```

Here is the the example of `slipstream.context`

```
[contextualization]
diid = <uuid-of-slipstream-run>
cookie = com.sixsq.slipstream.cookie=<cookie-content> Path:/
serviceurl = https://nuv.la
node_instance_name = orchestrator-exoscale-ch-gva
```

Require the run namespace and use the functions to interact with the run

```clojure
(require '[sixsq.slipstream.client.api.run :as r])

(r/get-state)
(r/action-scale-up "web-server" 3)
(r/terminate)
```

# License and copyright

Copyright (C) 2016 SixSq Sarl (sixsq.com)

The code in the public repositories is licensed under the Apache
license.

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License.  You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied.  See the License for the specific language governing
permissions and limitations under the License.

[ss-api]: http://ssapi.sixsq.com/

