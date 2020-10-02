import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion = properties["kotlinVersion"] as String
val karibuVersion = properties["karibuVersion"] as String
val vaadin8Version = properties["vaadin8Version"] as String

plugins {
  kotlin("jvm") version "1.4.10"
  id("org.gretty") version "2.3.1"
  id("com.devsoap.plugin.vaadin") version "2.0.0.beta2"
  war
}

defaultTasks("clean", "vaadinCompile", "build")

repositories {
  mavenCentral()
  maven {
    url = uri("https://maven.vaadin.com/vaadin-addons")
  }
}

defaultTasks("clean", "build")

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
  jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
  jvmTarget = "1.8"
}

vaadin {
  version = "8.9.4"
}

gretty {
  contextPath = "/"
  servletContainer = "jetty9.4"
}

dependencies {
  // Karibu-DSL dependency
  implementation("com.github.mvysny.karibudsl:karibu-dsl-v8:$karibuVersion")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  
  // logging
  // currently we are logging through the SLF4J API to LogBack. See src/main/resources/logback.xml file for the logger configuration
  implementation("ch.qos.logback:logback-classic:1.2.3")
  // this will allow us to configure Vaadin to log to SLF4J
  implementation("org.slf4j:jul-to-slf4j:1.7.25")
  
  
  implementation("com.vaadin:vaadin-themes:$vaadin8Version")
  implementation("com.vaadin:vaadin-server:$vaadin8Version")
  implementation("com.vaadin:vaadin-client-compiled:$vaadin8Version")
  implementation("javax.servlet:javax.servlet-api:3.1.0")
  
  //Dependencias do projeto
  implementation("org.sql2o:sql2o:1.5.4")
  implementation("mysql:mysql-connector-java:5.1.45")
  implementation("org.imgscalr:imgscalr-lib:4.2")
  
  implementation("org.vaadin.addons:vaadin-excel-exporter:2.0")
  
  implementation("de.steinwedel.vaadin.addon:messagebox:4.0.21")
  implementation("org.vaadin:viritin:2.5")

}

tasks.getByName<War>("war") {
  enabled = true
}
