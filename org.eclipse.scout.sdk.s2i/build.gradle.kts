/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

import org.jetbrains.intellij.tasks.PatchPluginXmlTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.Clock
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter
import java.util.*

val scoutSdkVersion = "11.0.0-SNAPSHOT"
val scoutSdkPluginVersion = scoutSdkVersion.replace("-SNAPSHOT", "." + timestamp())

val scoutRtVersion = projectPropertyOr("org.eclipse.scout.rt_version", "11.0-SNAPSHOT")
val javaVersion = JavaVersion.VERSION_11
val intellijVersion = projectPropertyOr("intellij_version", "IU-2020.1.2") // use "IU-LATEST-EAP-SNAPSHOT" to test against the latest IJ snapshot
val kotlinVersion = "1.3"

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
    id("org.jetbrains.intellij") version "0.5.1"
    kotlin("jvm") version "1.3.72"
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
    testImplementation("org.mockito", "mockito-core", "3.5.13")
    testImplementation("org.eclipse.scout.rt", "org.eclipse.scout.rt.client", scoutRtVersion)
    testImplementation("org.eclipse.scout.sdk", "org.eclipse.scout.sdk.core.test", scoutSdkVersion)
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version = intellijVersion
    downloadSources = true

    setPlugins("java", "maven", "copyright", "properties", "CSS", "JavaScriptLanguage")
    updateSinceUntilBuild = false

    tasks {
        withType<PatchPluginXmlTask> {
            version(scoutSdkPluginVersion)
        }
    }
}

allprojects {
    configure<JavaPluginConvention> {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
}

tasks.withType<org.jetbrains.intellij.tasks.PrepareSandboxTask> {
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
