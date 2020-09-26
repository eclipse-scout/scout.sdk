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
rootProject.name = "org.eclipse.scout.sdk.s2i"

// https://github.com/JetBrains/gradle-intellij-plugin/issues/537
pluginManagement {
    repositories {
        maven("https://jetbrains.bintray.com/intellij-plugin-service")
        maven("https://plugins.gradle.org/m2/")
        mavenCentral()
    }
}