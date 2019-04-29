# Release Process

## Prerequisites

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

## Pushing to Clojars

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
