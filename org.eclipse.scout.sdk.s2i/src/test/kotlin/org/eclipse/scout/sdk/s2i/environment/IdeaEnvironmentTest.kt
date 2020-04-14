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
package org.eclipse.scout.sdk.s2i.environment

import com.intellij.openapi.roots.LanguageLevelProjectExtension
import com.intellij.pom.java.LanguageLevel
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase
import com.intellij.testFramework.fixtures.ModuleFixture
import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator
import org.eclipse.scout.sdk.core.model.ecj.JreInfo
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.callInIdeaEnvironment


class IdeaEnvironmentTest : JavaCodeInsightFixtureTestCase() {

    override fun setUp() {
        super.setUp()
        LanguageLevelProjectExtension.getInstance(project).languageLevel = LanguageLevel.JDK_1_8
    }

    override fun tuneFixture(moduleBuilder: JavaModuleFixtureBuilder<ModuleFixture>) {
        super.tuneFixture(moduleBuilder)
        moduleBuilder
                .addJdk(JreInfo.getRunningJavaHome().toString())
                .setLanguageLevel(LanguageLevel.JDK_1_8)
    }


    fun testAsyncWriteOperationInheritsTransaction() {
        callInIdeaEnvironment(project, "Test") { env, progress ->
            val sourceFolder = env.toScoutJavaEnvironment(module)?.primarySourceFolder()?.orElse(null) ?: throw IllegalArgumentException("Could not find primary testing source folder")
            val generator = PrimaryTypeGenerator.create()
                    .withElementName("Test")
                    .withPackageName("a.b.c")
            env.writeCompilationUnitAsync(generator, sourceFolder, progress).awaitDoneThrowingOnError()

            assertEquals(1, TransactionManager.current().size()) // written compilation unit is registered in transaction of main thread
        }.awaitDoneThrowingOnError()
    }
}