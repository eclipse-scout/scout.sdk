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
package org.eclipse.scout.sdk.s2i.nls.inspection

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import org.eclipse.scout.sdk.core.s.environment.IEnvironment
import org.eclipse.scout.sdk.core.s.environment.IProgress
import org.eclipse.scout.sdk.core.s.nls.ITranslationStore
import org.eclipse.scout.sdk.core.s.nls.TranslationStores
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.callInIdeaEnvironmentSync
import org.eclipse.scout.sdk.s2i.environment.IdeaProgress
import org.eclipse.scout.sdk.s2i.environment.TransactionManager.Companion.runInNewTransaction
import java.util.stream.Stream

class RemoveUnusedTranslationQuickFix(val key: String, val store: ITranslationStore) : LocalQuickFix {

    private val m_familyName = message("remove.unused.translation")
    private val m_name = message("remove.unused.translation.x", key)

    override fun getFamilyName() = m_familyName

    override fun getName() = m_name

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val progress = IdeaProgress.currentOrEmpty()
        runInNewTransaction(project, m_name, { progress }) {
            callInIdeaEnvironmentSync(project, progress) { env, p ->
                removeTranslation(env, p)
            }
        }
    }

    private fun removeTranslation(env: IEnvironment, progress: IProgress) {
        val singleStoreStack = TranslationStores.createStack(Stream.of(store)).orElse(null) ?: return
        singleStoreStack.reload(progress) // in case the filesystem changed since the inspection was executed
        singleStoreStack.removeTranslations(Stream.of(key))
        singleStoreStack.flush(env, progress)
    }
}