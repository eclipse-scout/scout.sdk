/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import org.jetbrains.intellij.tasks.RunPluginVerifierTask.FailureLevel
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.Clock
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter
import java.util.*

val scoutSdkVersion = "13.0.0-SNAPSHOT"
val scoutSdkPluginVersion = "13.0.0.".plus(timestamp())

val javaVersion = JavaVersion.VERSION_17
val scoutRtVersion = projectPropertyOr("org.eclipse.scout.rt_version", "25.1-SNAPSHOT")
val intellijVersion = projectPropertyOr("intellij_version", "IU-2022.2.3") // use "IU-LATEST-EAP-SNAPSHOT" to test against the latest IJ snapshot

plugins {
    id("java")
    id("maven-publish")
    id("idea")
    id("org.jetbrains.intellij") version "1.17.4"
    kotlin("jvm") version "1.7.22"
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
    api("org.eclipse.scout.sdk", "org.eclipse.scout.sdk.core.java.ecj", scoutSdkVersion)
    api("org.apache.poi", "poi-ooxml", "5.3.0")
    testImplementation("org.mockito", "mockito-core", "5.13.0")
    testImplementation("org.eclipse.scout.rt", "org.eclipse.scout.rt.client", scoutRtVersion)
    testImplementation("org.eclipse.scout.sdk", "org.eclipse.scout.sdk.core.java.test", scoutSdkVersion)
    testImplementation("org.eclipse.scout.sdk", "org.eclipse.scout.sdk.core.typescript.test", scoutSdkVersion)
}

allprojects {
    java {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version.set(intellijVersion)
    downloadSources.set(true)
    plugins.set(listOf("java", "maven", "copyright", "properties", "JavaScriptLanguage" /* in newer IJ versions just called "JavaScript" */))
    updateSinceUntilBuild.set(false)
}

tasks {
    patchPluginXml {
        version.set(scoutSdkPluginVersion)
    }

    runPluginVerifier {
        ideVersions.set(listOf("IU-2022.3.3", "IU-2023.1.7", "IU-2023.2.8", "IU-2023.3.8", "IU-2024.1.6", "IU-2024.2.3", "IU-2024.3"))
        subsystemsToCheck.set("without-android")

        // all except EXPERIMENTAL_API_USAGES because of false positive in IJ 2024.2 with PsiExternalReferenceHost which is actually not marked as experimental.
        // can be removed as soon as the new intellij gradle plugin is used and the false positive is fixed in plugin verifier. Then FailureLevel.ALL should be used.
        failureLevel.set(
            listOf(
                FailureLevel.COMPATIBILITY_WARNINGS, FailureLevel.COMPATIBILITY_PROBLEMS, FailureLevel.DEPRECATED_API_USAGES, FailureLevel.SCHEDULED_FOR_REMOVAL_API_USAGES,
                FailureLevel.INTERNAL_API_USAGES, FailureLevel.OVERRIDE_ONLY_API_USAGES, FailureLevel.NON_EXTENDABLE_API_USAGES, FailureLevel.PLUGIN_STRUCTURE_WARNINGS, FailureLevel.MISSING_DEPENDENCIES, FailureLevel.INVALID_PLUGIN
            )
        )
    }

    withType<JavaCompile>().configureEach {
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
    }

    withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = javaVersion.toString()
        }
    }

    withType<Test> {
        // see com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
        // this Property allows AbstractTestCaseWithRunningClasspathModule to access all libraries of the running user classpath
        systemProperty("NO_FS_ROOTS_ACCESS_CHECK", project.findProperty("NO_FS_ROOTS_ACCESS_CHECK") ?: "true")
        systemProperty("file.encoding", "utf-8")
        ignoreFailures = true
    }

    jar {
        from("about.html")
    }
}

publishing {
    publications {
        // add plugin zip to publications so that it is included in the deployed artifacts
        create<MavenPublication>("mavenJava") {
            artifact("${layout.buildDirectory.asFile.get()}/distributions/$group-$version.zip")
            from(components["java"])
        }
    }
}

fun timestamp(): String {
    val now = now(Clock.systemUTC())
    // returned number must be a valid integer (not too big)
    return now.format(DateTimeFormatter.ofPattern("yyDDDHHmm", Locale.US))
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
