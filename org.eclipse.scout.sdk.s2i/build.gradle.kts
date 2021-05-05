/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

import org.jetbrains.intellij.tasks.PatchPluginXmlTask
import org.jetbrains.intellij.tasks.PrepareSandboxTask
import org.jetbrains.intellij.tasks.RunPluginVerifierTask
import org.jetbrains.intellij.tasks.RunPluginVerifierTask.FailureLevel
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.Clock
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter
import java.util.*

val scoutSdkVersion = "12.0.0-SNAPSHOT"
val scoutSdkPluginVersion = scoutSdkVersion.replace("-SNAPSHOT", "." + timestamp())

val kotlinVersion = "1.4"
val javaVersion = JavaVersion.VERSION_11
val scoutRtVersion = projectPropertyOr("org.eclipse.scout.rt_version", "22.0-SNAPSHOT")
val intellijVersion = projectPropertyOr("intellij_version", "IU-2021.1") // use "IU-LATEST-EAP-SNAPSHOT" to test against the latest IJ snapshot

fun timestamp(): String {
    val now = now(Clock.systemUTC())
    // returned number must be a valid integer (not too big)
    return now.format(DateTimeFormatter.ofPattern("yyMMddHHmm", Locale.US))
}

fun projectPropertyOr(propertyKey: String, defaultValue: String): String {
    val sysProp = System.getProperty(propertyKey)
    if (sysProp is String && sysProp.isNotBlank()) {
        return sysProp.trim()
    }
    val projectProp = project.findProperty(propertyKey)
    if (projectProp is String && projectProp.isNotBlank()) {
        return projectProp.trim()
    }
    return defaultValue
}

plugins {
    id("java")
    id("maven-publish")
    id("idea")
    id("org.jetbrains.intellij") version "0.7.3"
    kotlin("jvm") version "1.4.32"
    id("net.linguica.maven-settings") version "0.5" // for maven settings
}

group = "org.eclipse.scout.sdk.s2i"
version = scoutSdkVersion

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    api("org.eclipse.scout.sdk", "org.eclipse.scout.sdk.core.s", scoutSdkVersion)
    api("org.eclipse.scout.sdk", "org.eclipse.scout.sdk.core.ecj", scoutSdkVersion)
    api("org.apache.poi", "poi-ooxml", "4.1.2")
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    testImplementation("org.mockito", "mockito-core", "3.9.0")
    testImplementation("org.eclipse.scout.rt", "org.eclipse.scout.rt.client", scoutRtVersion)
    testImplementation("org.eclipse.scout.sdk", "org.eclipse.scout.sdk.core.test", scoutSdkVersion)
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version = intellijVersion
    downloadSources = true

    setPlugins("java", "maven", "copyright", "properties", "CSS", "JavaScriptLanguage")
    updateSinceUntilBuild = false

    tasks.withType<PatchPluginXmlTask> {
        version(scoutSdkPluginVersion)
    }

    tasks.withType<RunPluginVerifierTask> {
        setIdeVersions(listOf("IU-2021.1.1"))
        setSubsystemsToCheck("without-android")
        failureLevel = FailureLevel.ALL
    }
}

allprojects {
    configure<JavaPluginConvention> {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
}

tasks.withType<PrepareSandboxTask> {
    // prepareSandbox Task may copy duplicate libraries from transitive dependencies.
    // See https://intellij-support.jetbrains.com/hc/en-us/community/posts/360009478700-Kotlin-Getting-Copying-or-archiving-duplicate-paths-deprecation-warnings-when-building-plugin
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = javaVersion.toString()
    targetCompatibility = javaVersion.toString()
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = javaVersion.toString()
        apiVersion = kotlinVersion
        languageVersion = kotlinVersion
    }
}

tasks.withType<Test> {
    // see com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
    // this Property allows AbstractTestCaseWithRunningClasspathModule to access all libraries of the running user classpath
    systemProperty("NO_FS_ROOTS_ACCESS_CHECK", project.findProperty("NO_FS_ROOTS_ACCESS_CHECK") ?: "true")
    systemProperty("file.encoding", "utf-8")
    ignoreFailures = true
}

tasks.jar {
    from("about.html")
    from("epl-v10.html")
}

publishing {
    publications {
        // add plugin zip to publications so that it is included in the deployed artifacts
        create<MavenPublication>("mavenJava") {
            artifact("$buildDir/distributions/$group-$version.zip")
            from(components["java"])
        }
    }
}
