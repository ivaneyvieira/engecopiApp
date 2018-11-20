[![Build Status](https://travis-ci.org/mvysny/karibu-helloworld-application.svg?branch=master)](https://travis-ci.org/mvysny/karibu-helloworld-application)
[![Heroku](https://heroku-badge.herokuapp.com/?app=karibu-helloworld-app&style=flat&svg=1)](https://karibu-helloworld-app.herokuapp.com/)

# Karibu-DSL Example App / Archetype

Template for a simple Kotlin-DSL application that only requires a Servlet 3.0 container to run.
Just clone this repo and start experimenting!

Uses [Karibu-DSL](https://github.com/mvysny/karibu-dsl); for more information about the
Karibu-DSL framework please see https://github.com/mvysny/karibu-dsl .
For more information on Vaadin please see https://vaadin.com/docs/-/part/framework/tutorial.html

# Getting Started

To quickly start the app, make sure that you have Java 8 JDK installed. Then, just type this into your terminal:

```bash
git clone https://github.com/mvysny/karibu-helloworld-application
cd karibu-helloworld-application
./gradlew build appRun
```

The app will be running on [http://localhost:8080/](http://localhost:8080/)

Since the build system is a Gradle file written in Kotlin, we suggest you use [Intellij IDEA](https://www.jetbrains.com/idea/download)
to edit the project files. The Community edition is enough to run the server
via Gretty's `./gradlew appRun`. The Ultimate edition will allow you to run the project in Tomcat - this is the recommended
option for a real development.

# Workflow

To compile the entire project, run `./gradlew`.

To run the application, run `./gradlew appRun` and open [http://localhost:8080/](http://localhost:8080/) .

To produce a deployable production mode WAR:
- change `productionMode` to `true` in the servlet class configuration (located in the [MyUI.kt](src/main/kotlin/org/test/MyUI.kt) file)
- run `./gradlew`
- You will find the WAR file in `build/libs/karibu-helloworld-application.war`

This will allow you to quickly start the example app and allow you to do some basic modifications.

## Client-Side compilation

The project is using an automatically generated widgetset by default. 
When you add a dependency that needs client-side compilation, the Vaadin Gradle plugin will 
automatically generate it for you. Your own client-side customisations can be added into
package "client".

Debugging client side code  @todo revisit with Gradle
  - run "mvn vaadin:run-codeserver" on a separate console while the application is running
  - activate Super Dev Mode in the debug window of the application

## Developing a theme using the runtime compiler

When developing the theme, Vaadin can be configured to compile the SASS based
theme at runtime in the server. This way you can just modify the scss files in
your IDE and reload the browser to see changes.

To use the runtime compilation, run `./gradlew clean appRun`. Gretty will automatically
pick up changes in theme files and Vaadin will automatically compile the theme on
browser refresh. You will just have to give Gretty some time (one second) to register
the change.

When using the runtime compiler, running the application in the "run" mode 
(rather than in "debug" mode) can speed up consecutive theme compilations
significantly.

It is highly recommended to disable runtime compilation for production WAR files.

# Development with Intellij IDEA Ultimate

The easiest way (and the recommended way) to develop Karibu-DSL-based web applications is to use Intellij IDEA Ultimate.
It includes support for launching your project in any servlet container (Tomcat is recommended)
and allows you to debug the code, modify the code and hot-redeploy the code into the running Tomcat
instance, without having to restart Tomcat.

1. First, download Tomcat and register it into your Intellij IDEA properly: https://www.jetbrains.com/help/idea/2017.1/defining-application-servers-in-intellij-idea.html
2. Then just open this project in Intellij, simply by selecting `File / Open...` and click on the
   `build.gradle` file. When asked, select "Open as Project".
2. You can then create a launch configuration which will launch this example app in Tomcat with Intellij: just
   scroll to the end of this tutorial: https://kotlinlang.org/docs/tutorials/httpservlets.html
3. Start your newly created launch configuration in Debug mode. This way, you can modify the code
   and press `Ctrl+F9` to hot-redeploy the code. This only redeploys java code though, to
   redeploy resources just press `Ctrl+F10` and select "Update classes and resources"

## Dissection of project files

Let's look at all files that this project is composed of, and what are the points where you'll add functionality:

| Files | Meaning
| ----- | -------
| [build.gradle.kts](build.gradle.kts) | [Gradle](https://gradle.org/) build tool configuration files. Gradle is used to compile your app, download all dependency jars and build a war file
| [gradlew](gradlew), [gradlew.bat](gradlew.bat), [gradle/](gradle) | Gradle runtime files, so that you can build your app from command-line simply by running `./gradlew`, without having to download and install Gradle distribution yourself.
| [.travis.yml](.travis.yml) | Configuration file for [Travis-CI](http://travis-ci.org/) which tells Travis how to build the app. Travis watches your repo; it automatically builds your app and runs all the tests after every commit.
| [.gitignore](.gitignore) | Tells [Git](https://git-scm.com/) to ignore files that can be produced from your app's sources - be it files produced by Gradle, Intellij project files etc.
| [src/main/resources/](src/main/resources) | A bunch of static files not compiled by Kotlin in any way; see below for explanation.
| [logback.xml](src/main/resources/logback.xml) | We're using [Slf4j](https://www.slf4j.org/) for logging and this is the configuration file for Slf4j
| [webapp/](src/main/webapp) | static files provided as-is to the browser. See below for explanation
| [mytheme/](src/main/webapp/VAADIN/themes/mytheme) | Vaadin Theme which is generally a bunch of SCSS files compiled to one large CSS. Read more at [Creating and Using Themes](https://vaadin.com/docs/v8/framework/themes/themes-creating.html)
| [src/main/kotlin/](src/main/kotlin) | The main Kotlin sources of your web app. You'll be mostly editing files located in this folder.
| [MyUI.kt](src/main/kotlin/org/test/MyUI.kt) | When Servlet Container (such as Tomcat) starts your app, it will show the components attached to the main `UI` class, or in this case, the `MyUI` class. The `MyUIServlet` defines which UI to use and where to map the application to.
| [MyUITest.kt](src/test/kotlin/org/test/MyUITest.kt) | Automatically run by Gradle to test your UI; see [Karibu Testing](https://github.com/mvysny/karibu-testing) for more information.

# More Resources

* The DSL technique is used to allow you to nest your components in a structured code. This is provided by the
  Karibu-DSL library; please visit the [Karibu-DSL home page](https://github.com/mvysny/karibu-dsl) for more information.
* The browserless testing is demonstrated in the [MyUITest.kt](src/test/kotlin/org/test/MyUITest.kt) file.
  Please read [Browserless Web Testing](https://github.com/mvysny/karibu-testing) for more information.
* For more complex example which includes multiple pages, please see the [Karibu-DSL example-v8 app](https://github.com/mvysny/karibu-dsl#quickstart).
* For information on how to connect the UI to the database backend please visit [Vaadin-on-Kotlin](http://www.vaadinonkotlin.eu/)
  You can find a complete CRUD example at [Vaadin-on-Kotlin vok-example-crud-sql2o](https://github.com/mvysny/vaadin-on-kotlin#example-project).
