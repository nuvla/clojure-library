# Nuvla Clojure Client Library

[![Build Status](https://travis-ci.com/nuvla/clojure-library.svg?branch=master)](https://travis-ci.com/nuvla/clojure-library)

Nuvla client library to facilitate interaction with the Nuvla REST API
via the Clojure language.

For usage information, view the online
[cljdoc](https://cljdoc.org/d/sixsq.nuvla/api) documentation or see
the `doc` directory and source files.

## Artifacts

[![Clojars Project](https://img.shields.io/clojars/v/sixsq.nuvla/api.svg)](https://clojars.org/sixsq.nuvla/api) 

## Contributing

### Source Code Changes

To contribute code to this repository, please follow these steps:

 1. Create a branch from master with a descriptive, kebab-cased name
    to hold all your changes.

 2. Follow the developer guidelines concerning formatting, etc. when
    modifying the code.
   
 3. Once the changes are ready to be reviewed, create a GitHub pull
    request.  With the pull request, provide a description of the
    changes and links to any relevant issues (in this repository or
    others). 
   
 4. Ensure that the triggered CI checks all pass.  These are triggered
    automatically with the results shown directly in the pull request.

 5. Once the checks pass, assign the pull request to the repository
    coordinator (who may then assign it to someone else).

 6. Interact with the reviewer to address any comments.

When the reviewer is happy with the pull request, he/she will "squash
& merge" the pull request and delete the corresponding branch.

**Be aware that this code is intended to work with both Clojure and
ClojureScript.** Keep the source code compatible with both as much as
possible. 

### Testing

Add appropriate tests that verify the changes or additions you make to
the source code.

### Code Formatting

The bulk of the code in this repository is written in Clojure(Script).

The formatting follows the standard formatting provided by the Cursive
IntelliJ plugin with all the default settings **except that map
entries should be aligned**.

Additional, formatting guidelines, not handled by the Cursive plugin:

 - Use a new line after the `:require` and `:import` keys in namespace
   declarations.

 - Alphabetize the required namespaces.  This can be automated with
   `lein nsorg --replace`.

 - Use 2 blank lines between top-level forms.

 - Use a single blank line between a block comment and the following
   code.

IntelliJ (with Cursive) can format easily whole directories of source
code.  Do not hesitate to use this feature to keep the source code
formatting standardized.

# Copyright

Copyright &copy; 2019-2021 SixSq Sàrl

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
