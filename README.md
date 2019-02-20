# Nuvla Clojure Client Library

Nuvla clojure client library to interact with the Nuvla REST API.

## Building

To build everything, just do the usual maven dance:

```sh
mvn clean install
```

This will trigger the full build via leiningen.  You can also do the
build directly with leiningen:

```sh
lein do clean, test, install
```

All artifacts will be put into your local maven repository.  

## Deploying Artifacts

To push (deploy) the build artifacts to the maven repository hosted in
AWS S3, you must define the following environmental variables:

 - AWS_SECRET_ACCESS_KEY: value of the AWS secret key
 - AWS_ACCESS_KEY_ID: value of the AWS access key

To add these to the Travis CI configuration, the commands are:

```sh
travis encrypt AWS_SECRET_ACCESS_KEY=<value> --add env.global --com
travis encrypt AWS_ACCESS_KEY_ID=<value> --add env.global --com 
```

The updated `.travis.yml` file will need to be pushed into the git
repository.  The same commands can be used to change the credentials.
Be sure to delete the previous definitions from the `.travis.yml`
file.

# Copyright

Copyright &copy; 2019 SixSq Sàrl

# License

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License.  You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied.  See the License for the specific language governing
permissions and limitations under the License.
