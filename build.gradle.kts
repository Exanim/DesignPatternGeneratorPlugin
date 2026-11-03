plugins {
    kotlin("jvm") version "2.3.20"
    id("org.jetbrains.intellij.platform") version "2.10.4"
}

val pluginGroup: String by project
val pluginVersion: String by project
val pluginSinceBuild: String by project
val platformVersion: String by project // e.g. 2024.3.6
val platformType: String by project // e.g. IC

group = pluginGroup
version = pluginVersion

kotlin { jvmToolchain(21) }

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

intellijPlatform {
    // Build / instrumentation options
    buildSearchableOptions = false
    instrumentCode = true
    sandboxContainer = file(".sandbox")
    pluginVerification {}
}

defaultTasks("assemble")

dependencies {
    intellijPlatform {
        create(platformType, platformVersion)
        bundledPlugins("com.intellij.java", "org.jetbrains.kotlin")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
}

tasks.test {
    useJUnitPlatform()
}
