/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.extensions.intellijPlatform
import org.jetbrains.intellij.platform.gradle.models.ProductRelease
import org.jetbrains.intellij.platform.gradle.tasks.RunIdeTask
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.time.Clock
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter
import java.util.*

val scoutSdkVersion = "14.0.0-SNAPSHOT"
val scoutSdkPluginVersion = "14.0.0.".plus(timestamp())
val scoutRtVersion = projectPropertyOr("org.eclipse.scout.rt_version", "25.2-SNAPSHOT")

plugins {
    id("java")
    id("maven-publish")

    // See https://github.com/JetBrains/intellij-platform-gradle-plugin
    id("org.jetbrains.intellij.platform") version "2.5.0" // do not use 2.6.0 or 2.7.0. It is not compatible with IJ 2024.3

    kotlin("jvm") version "2.1.21"
    id("io.github.rmanibus.maven-settings") version "0.8" // for maven settings
}

group = "org.eclipse.scout.sdk.s2i"
version = scoutSdkVersion

repositories {
    mavenLocal()
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    api("org.eclipse.scout.sdk", "org.eclipse.scout.sdk.core.s", scoutSdkVersion)
    api("org.eclipse.scout.sdk", "org.eclipse.scout.sdk.core.java.ecj", scoutSdkVersion)
    implementation("org.apache.poi", "poi-ooxml", "5.4.1")
    testImplementation("org.mockito", "mockito-core", "5.18.0")
    testImplementation("junit", "junit", "4.13.2")
    testImplementation("org.eclipse.scout.rt", "org.eclipse.scout.rt.client", scoutRtVersion)
    testImplementation("org.eclipse.scout.sdk", "org.eclipse.scout.sdk.core.java.test", scoutSdkVersion)
    testImplementation("org.eclipse.scout.sdk", "org.eclipse.scout.sdk.core.typescript.test", scoutSdkVersion)

    intellijPlatform {
        intellijIdeaUltimate("2024.3.3")
        bundledPlugins(listOf("com.intellij.java", "org.jetbrains.idea.maven", "com.intellij.copyright", "com.intellij.properties", "JavaScript"))
        pluginVerifier()
        testFramework(TestFrameworkType.Platform)
        testFramework(TestFrameworkType.JUnit5)
        testFramework(TestFrameworkType.Plugin.Java)
        testFramework(TestFrameworkType.Plugin.JavaScript)
        testFramework(TestFrameworkType.Plugin.Maven)
        testFramework(TestFrameworkType.Bundled)
    }
}

allprojects {
    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

intellijPlatform {
    pluginConfiguration {
        version = scoutSdkPluginVersion
        ideaVersion {
            sinceBuild = "243"
            untilBuild = provider { null }
        }
    }
    pluginVerification {
        failureLevel = VerifyPluginTask.FailureLevel.ALL
        verificationReportsFormats = VerifyPluginTask.VerificationReportsFormats.ALL
        subsystemsToCheck = VerifyPluginTask.Subsystems.WITHOUT_ANDROID
        ides {
            select {
                types = listOf(IntelliJPlatformType.IntellijIdeaUltimate)
                channels = listOf(ProductRelease.Channel.RELEASE)
                sinceBuild = "2025.1.4.1"
            }
            select {
                types = listOf(IntelliJPlatformType.IntellijIdeaUltimate)
                channels = listOf(ProductRelease.Channel.EAP)
                sinceBuild = "252"
            }
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        sourceCompatibility = JavaVersion.VERSION_21.toString()
        targetCompatibility = JavaVersion.VERSION_21.toString()
    }

    withType<VerifyPluginTask> {
        // increase memory for plugin verifier fork as the default value is no longer enough
        maxHeapSize = "3g"
    }

    withType<RunIdeTask> {
        maxHeapSize = "5g"
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
