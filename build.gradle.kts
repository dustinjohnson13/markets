import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    groovy
    kotlin("jvm") version "1.3.21"
    application
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

val groovyVersion = "2.4.8"
val slf4jVersion = "1.7.25"
val spockVersion = "1.1-groovy-2.4-rc-3"

dependencies {
    api("com.google.guava:guava:23.0")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("com.oanda.v20:v20:3.0.25")
    implementation("com.tictactec:ta-lib:0.4.0")


    testImplementation("org.codehaus.groovy:groovy-all:$groovyVersion")
    testImplementation("org.spockframework:spock-core:$spockVersion") {
        exclude(group = "org.codehaus.groovy")
    }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

tasks.getByName<Jar>("jar") {
    version = ""
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.withType<Test> {
    maxHeapSize = "4096m"
}

application {
    mainClassName = "markets.live.Runner"
}