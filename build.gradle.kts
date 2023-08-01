import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion = properties["kotlinVersion"] as String
val karibuVersion = properties["karibuVersion"] as String
val vaadin8Version = properties["vaadin8Version"] as String

plugins {
  kotlin("jvm") version "1.4.20"
  id("org.gretty") version "3.0.3"
  id("com.devsoap.plugin.vaadin") version "2.0.0.beta2"
}

defaultTasks("clean", "vaadinCompile", "build")

repositories {
  jcenter()
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "1.8"
}

vaadin {
  version = properties["vaadin8Version"] as String?
}

gretty {
  contextPath = "/"
  servletContainer = "jetty9.4"
}

dependencies {
  // Karibu-DSL dependency
  compile("com.github.mvysny.karibudsl:karibu-dsl-v8:1.0.4")
  compile(kotlin("stdlib-jdk8"))
  compile(kotlin("reflect"))
  
  // logging
  // currently we are logging through the SLF4J API to SLF4J-Simple. See src/main/resources/simplelogger.properties file for the logger configuration
  compile("org.slf4j:slf4j-simple:1.7.30")
  // this will allow us to configure Vaadin to log to SLF4J
  compile("org.slf4j:jul-to-slf4j:1.7.30")
  
  
  compile("com.vaadin:vaadin-themes:$vaadin8Version")
  compile("com.vaadin:vaadin-client-compiled:$vaadin8Version")
  
  //Dependencias do projeto
  compile("org.sql2o:sql2o:1.5.4")
  compile("mysql:mysql-connector-java:5.1.48")
  compile("org.imgscalr:imgscalr-lib:4.2")
  
  compile("org.vaadin.addons:vaadin-excel-exporter:2.0")
  
  compile("de.steinwedel.vaadin.addon:messagebox:4.0.21")
  compile("org.vaadin:viritin:2.5")
}

