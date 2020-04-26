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

import org.jetbrains.intellij.tasks.*
import org.jetbrains.kotlin.gradle.tasks.*
import java.time.Clock
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter

val SCOUT_SDK_VERSION = "10.0.0-SNAPSHOT"
val SCOUT_SDK_PLUGIN_VERSION = SCOUT_SDK_VERSION.replace("-SNAPSHOT", "." + timestamp())

fun timestamp(): String {
    val now = now(Clock.systemUTC())
    // returned number must be a valid integer (not too big)
    return now.format(DateTimeFormatter.ofPattern("yyMMddHHmm"))
}

plugins {
    id("java")
    id("maven-publish")
    id("idea")
    id("org.jetbrains.intellij") version "0.4.18"
    kotlin("jvm") version "1.3.61"
}

group = "org.eclipse.scout.sdk.s2i"
version = SCOUT_SDK_VERSION

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    api("org.eclipse.scout.sdk", "org.eclipse.scout.sdk.core.s", SCOUT_SDK_VERSION)
    api("org.eclipse.scout.sdk", "org.eclipse.scout.sdk.core.ecj", SCOUT_SDK_VERSION)
    implementation("org.jetbrains.kotlin", "kotlin-stdlib-jdk8", "1.3.61")
    testImplementation("org.mockito", "mockito-core", "3.3.3")
}


// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version = "IU-2019.2.3"
    downloadSources = true

    setPlugins("java", "maven", "copyright", "properties")
    updateSinceUntilBuild = false

    tasks {
        withType<PatchPluginXmlTask> {
            version(SCOUT_SDK_PLUGIN_VERSION)
        }
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}

tasks.withType<KotlinCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
    kotlinOptions {
        jvmTarget = "1.8"
        apiVersion = "1.3"
        languageVersion = "1.3"
    }
}

// https://docs.gradle.org/current/dsl/org.gradle.plugins.ide.idea.model.IdeaModule.html
idea {
    module {
        // Fix problems caused by separate output directories for classes/resources in IntelliJ IDEA
        inheritOutputDirs = true
    }
}

tasks.jar {
    from("about.html")
    from("epl-v10.html")
}