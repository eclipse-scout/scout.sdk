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
package org.eclipse.scout.sdk.s2i.environment

import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator
import org.eclipse.scout.sdk.s2i.AbstractTestCaseWithRunningClasspathModule
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.callInIdeaEnvironment


class IdeaEnvironmentTest : AbstractTestCaseWithRunningClasspathModule() {

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