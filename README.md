# Nuvla Clojure Client Library

Nuvla client library to facilitate interaction with the Nuvla REST API
via the Clojure language.

For usage information, view the online
[cljdoc](https://cljdoc.org/d/sixsq.nuvla/api) documentation or see
the `doc` directory and source files.

## Artifacts

 - `sixsq.nuvla/api` jar file: Jar file containing the Nuvla Clojure
   API.  Available from
   [clojars](https://clojars.org/sixsq.nuvla/api).

## Release Process

### Prerequisites

To build and release this `project.clj` file to
[clojars](https://clojars.org), you must setup your environment
correctly.

First, set the username and password of your clojars account as
environmental variables:

    export CLOJARS_USERNAME=username
    export CLOJARS_PASSWORD=password

These will be used when pushing the artifacts to clojars.  For the
SixSq release account, the username and password are in 1Password.

Non-snapshot releases of artifacts also must be signed with a GPG key
when uploading to clojars.  You must install GPG and then import the
SixSq Release GPG keys.  The public key, private key, and password are
all in 1Password.

For GPG, set the environmental variable:

    export GPG_TTY=$(tty)

This should then allow GPG to prompt for the password for the private
key. If you run into an error like the following:

    gpg: signing failed: Inappropriate ioctl for device

The problem is with the TTY that GPG is trying to use and usually
means that the above environmental variable is not set.

### Pushing to Clojars

**Before** creating the release:

 - **Be sure that you've done all of the setup in the previous
   section!**

 - Decide what semantic version to use for this release and change the
   version in `project.clj`. (It should still have the "-SNAPSHOT"
   suffix.) 

 - Update the changelog.

In general, use the following guidelines when choosing how to change
the version:

 - :patch for changes that are strictly backwards-compatible,
   e.g. adding new dependencies
 - :minor for changes that change the versions of existing
   dependencies or delete dependencies
 - :major for major changes such as changing repository definitions

Again, be sure to set the version **before** tagging the release.

Check that everything builds correctly with:

    lein do clean, test, jar, install

Ensure that all changes are checked into GitHub.  The release will
fail if there are local changes.

To tag the code and release the jar to clojars, just run the command:

    lein release :patch

This will do everything necessary and will bump the patch version of
the artifact at the end of the process. You will be prompted for the
passphrase of the GPG key.

After releasing a new version on clojars, you should check that the
jar is available from clojars (`sixsq.nuvla/api`) and then communicate
the availability of the new release to the coordinators of dependent
components.

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
