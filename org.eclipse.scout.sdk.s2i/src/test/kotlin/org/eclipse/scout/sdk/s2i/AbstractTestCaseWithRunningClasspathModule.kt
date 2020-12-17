/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i

import com.intellij.openapi.roots.LanguageLevelProjectExtension
import com.intellij.pom.java.LanguageLevel
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase
import com.intellij.util.io.isFile
import org.eclipse.scout.sdk.core.model.ecj.JreInfo
import org.eclipse.scout.sdk.core.model.ecj.JreInfo.runningUserClassPath
import org.eclipse.scout.sdk.core.util.Ensure.newFail

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
        runningUserClassPath(jreHome)
                .filter { it.isFile() }
                .forEach { moduleBuilder.addLibrary(it.fileName.toString(), it.toString()) }
    }
}