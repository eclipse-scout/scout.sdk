/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i

import com.intellij.openapi.roots.LanguageLevelProjectExtension
import com.intellij.pom.java.LanguageLevel
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase
import org.eclipse.scout.sdk.core.java.ecj.JreInfo
import org.eclipse.scout.sdk.core.util.Ensure.newFail
import kotlin.io.path.isRegularFile

abstract class AbstractTestCaseWithRunningClasspathModule : JavaCodeInsightFixtureTestCase() {

    private val m_jreInfo = JreInfo.runningJreInfo()
    private val m_javaLanguageLevel = LanguageLevel.parse(m_jreInfo.version()) ?: throw newFail("Unknown Java version: '{}'.", m_jreInfo.version())

    override fun setUp() {
        super.setUp()
        LanguageLevelProjectExtension.getInstance(project).languageLevel = m_javaLanguageLevel
    }

    override fun tuneFixture(moduleBuilder: JavaModuleFixtureBuilder<*>) {
        super.tuneFixture(moduleBuilder)

        val jreHome = m_jreInfo.jreHome()
        moduleBuilder
            .addJdk(jreHome.toString())
            .setLanguageLevel(m_javaLanguageLevel)
        JreInfo.runningUserClassPath(jreHome)
            .filter { it.isRegularFile() }
            .forEach { moduleBuilder.addLibrary(it.fileName.toString(), it.toString()) }
    }
}