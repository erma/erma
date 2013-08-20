ERMA (Extremely Reusable Monitoring API)
========================================

ERMA is an instrumentation API that has been designed to be applicable for all monitoring needs. The
design goal is "to make instrumentation as simple as logging." Lowering the effort needed to add
instrumentation will encourage developers to add monitoring code where ever it makes sense.

Resources
---------

* [Javadoc API documentation](http://erma.github.com/erma/)
* [Wiki](http://github.com/erma/erma/wiki)
* [Code](http://github.com/erma/erma)
* [Issue Tracking](http://orbitz-oss.atlassian.net/browse/ERMA)
* [Mailing list](http://groups.google.com/group/erma-core)

Build Instructions
------------------

ERMA is built with [Gradle](http://www.gradle.org/).  You can get started quickly by using the
included Gradle wrapper like this:

    ./gradlew

Gradle will automatically be downloaded from the location specified in gradle-wrapper.properties and
installed.  You may also run targets from an existing Gradle installation if you choose (in this
case substitute `gradle` for `./gradlew` to execute the tasks).

Code
----

The codebase is divided into multiple subprojects: **erma-api**, **erma-lib**, **erma-test**, and extensions located
in the **erma-ext** folder, such as **erma-mongo-processor**.

* **erma-api**
    * The basic constructs used to instrument code.
* **erma-lib**
    * Objects one would typically wire into an application to configure the MonitoringEngine.
* **erma-test**
    * Objects one would typically wire into an application to configure the MonitoringEngine to test intrumentation.
* **erma-ext**
    * Extend ERMA to integrate with other third-party projects.

Build Targets
-------------

List available tasks:

    ./gradlew -t

Generate Javadocs in build/docs/:

    ./gradlew javadoc

Build all jars, run JUnit tests and generate JUnit reports in each subproject build/reports/tests/:

    ./gradlew build
