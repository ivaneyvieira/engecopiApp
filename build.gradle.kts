import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
  kotlin("jvm") version "1.2.41"
  // need to use Gretty here because of https://github.com/johndevs/gradle-vaadin-plugin/issues/317
  id("org.gretty") version "2.1.0"
  id("com.devsoap.plugin.vaadin") version "1.3.1"
}

defaultTasks("clean", "build")

repositories {
  mavenCentral()
  jcenter()
  maven("https://dl.bintray.com/mvysny/github")
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "1.8"
}

vaadin {
  version = "8.4.2"
}

gretty {
  contextPath = "/"
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    // to see the exceptions of failed tests in Travis-CI console.
    exceptionFormat = TestExceptionFormat.FULL
  }
}

dependencies {
  // Karibu-DSL dependency
  compile("com.github.vok.karibudsl:karibu-dsl-v8:0.4.5")
  
  // include proper kotlin version
  compile(kotlin("stdlib-jdk8"))
  
  // logging
  // currently we are logging through the SLF4J API to LogBack. See src/main/resources/logback.xml file for the logger configuration
  compile("ch.qos.logback:logback-classic:1.2.3")
  // this will allow us to configure Vaadin to log to SLF4J
  compile("org.slf4j:jul-to-slf4j:1.7.25")
  
  // test support
  testCompile("com.github.kaributesting:karibu-testing-v8:0.4.14")
  testCompile("com.github.mvysny.dynatest:dynatest:0.8")
  
  // workaround until https://youtrack.jetbrains.com/issue/IDEA-178071 is fixed
  compile("com.vaadin:vaadin-themes:${vaadin.version}")
  compile("com.vaadin:vaadin-client-compiled:${vaadin.version}")
  
  //Dependencias do projeto
  compile("org.sql2o:sql2o:1.5.4")
  compile("mysql:mysql-connector-java:5.1.45")
  compile("org.imgscalr:imgscalr-lib:4.2")
  
  compile("org.vaadin.addons:vaadin-excel-exporter:2.0")
  
  compile("de.steinwedel.vaadin.addon:messagebox:4.0.21")
  compile("org.vaadin:viritin:2.5")
  // heroku app runner
  testRuntime("com.github.jsimone:webapp-runner:8.5.30.0")
}

// Heroku
tasks {
  "copyToLib"(Copy::class) {
    into("$buildDir/server")
    from(configurations.testRuntime) {
      include("webapp-runner*")
    }
  }
  "stage" {
    dependsOn("build", "copyToLib")
  }
}
