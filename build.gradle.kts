import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


val kotlinVersion = "1.3.31"
buildscript {
    repositories {
        google()
        jcenter()

    }
}

val ktor_version = "1.2.4"

plugins {
    java
    kotlin("jvm") version "1.3.31"
    application
}

group = "com.vhl"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testCompile("junit", "junit", "4.12")


    //ktor
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor:$ktor_version")
    implementation("io.ktor:ktor-websockets:$ktor_version")
    implementation( "io.ktor:ktor-gson:$ktor_version")

    //logging
    implementation("io.github.microutils:kotlin-logging:1.6.24")
    implementation("ch.qos.logback:logback-classic:1.2.3")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

configure<ApplicationPluginConvention> {
    mainClassName = "com.proto.MainAppKt"
}