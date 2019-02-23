# Developer Information

## Source Code

The source code is managed through the
[nuvla/clojure-library](https://github.com/nuvla/clojure-library)
GitHub repository. Clone the repository from these to obtain a copy of
the source code.

## Building

[Leiningen](https://leiningen.org/) is used to build the library.  If
you do not already have it installed, then visit its website for
installation instructions.

To run the full build, run the following command at the top of the
repository: 

```sh
lein do clean, test, install
```

All generated artifacts will be put into your local maven repository.

The repository is linked to the Travis continuous integration system,
so all changes checked into the GitHub repository will be built
automatically on Travis.  The [build
results](https://travis-ci.com/nuvla/clojure-library) can be found on
the Travis dashboards.

## Deploying Artifacts

To deploy the artifacts to the development maven repositories, then
run the command:

```sh
lein deploy
```

To successfully push (deploy) artifacts to these repositories, you
must have access credentials for the SixSq AWS S3 buckets.  Define the
following environmental variables:

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

## Documentation

The documentation will be built automatically from the source code
(and files in the `doc` directory) when a release has been pushed to
clojars.

To run the cljdoc server locally to verify the contents and rendering
of the documentation, see the cljdoc documentation on [running the
server
locally](https://github.com/cljdoc/cljdoc/blob/master/doc/running-cljdoc-locally.md).

The "ingest" command you will use looks something like this:

    ./script/cljdoc ingest -p sixsq.nuvla/clojure-library \
                           -v 0.0.1-SNAPSHOT \
                           --jar ~/.m2/reposiory/sixsq/nuvla/clojure-library/0.0.1-SNAPSHOT/clojure-library-0.0.1-SNAPSHOT.jar \
                           --pom ~/.m2/repository/sixsq/nuvla/clojure-library/0.0.1-SNAPSHOT/clojure-library-0.0.1-SNAPSHOT.pom \
                           --git ../clojure-library \
                           --rev "master"

Adjust the command as necessary. **To build from the installed jar
file, you must run `lein install` beforehand!**The ingestion takes a
significant amount of time.

If you build the version "0.0.1-SNAPSHOT", the documentation will
appear at the URL
http://localhost:8000/d/sixsq.nuvla/clojure-library/0.0.1-SNAPSHOT. 

## Releasing

### Prerequisites

 - Ensure that the code builds correctly and that all of the unit
   tests pass.
 - Verify that the documentation has been updated and that it renders
   correctly (see above).
 - Provide a summary of changes in the CHANGELOG (before releasing!). 

### Setup

To release this library to [clojars](https://clojars.org), you must
setup your environment correctly.

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

### Push Release

**Be sure that you've done all of the setup in the previous section!**

To release this to clojars, just run the command:

    lein release :patch

This will bump the patch version of the artifact.

In general, use the following guidelines when choosing how to change
the version:

 - :patch for changes that are strictly backwards-compatible,
   e.g. adding new dependencies
 - :minor for changes that change the versions of existing
   dependencies or delete dependencies
 - :major for major changes such as changing repository definitions

After releasing, ensure that the new version appears on
[clojars](https://clojars.org) and that the documentation is built
correctly on [cljdoc](https://cljdoc.org).
