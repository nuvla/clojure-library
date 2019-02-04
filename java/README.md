Using CIMI API from Java
========================

This repository contains an example for calling the Clojure CIMI API
from Java.

Java Interface
--------------

The class `sixsq.slipstream.client.CIMI` provides a lightweight
wrapper around the Clojure `sixsq.slipstream.client.api.cimi` protocol
(interface) to make use the API a bit more idiomatic from Java.

Simple Example
--------------

The example (`CIMIExample.java`) shows how to authenticate with the
server (using username and password) and then runs through the
lifecycle of a created SSH key pair.

To compile the code, just do the usual maven dance:

```
mvn clean install
```

This will generate an executable uberjar.  You can execute the example
with:

```
java -jar ./target/java-example-1.0.jar  $USERNAME $PASSWORD
```

where you've setup the USERNAME and PASSWORD environmental variables
with your Nuvla username and password, respectively.

Feedback
--------

Please provide feedback on this example by submitting issues to this
GitHub repository. All suggestions are welcome, particularly those
that make using the API easier from Java.
