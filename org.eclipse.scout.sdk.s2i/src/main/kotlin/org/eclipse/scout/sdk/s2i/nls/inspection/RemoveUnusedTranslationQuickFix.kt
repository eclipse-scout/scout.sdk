/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.nls.inspection

import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import org.eclipse.scout.sdk.core.s.environment.IEnvironment
import org.eclipse.scout.sdk.core.s.environment.IProgress
import org.eclipse.scout.sdk.core.s.nls.ITranslationStore
import org.eclipse.scout.sdk.core.s.nls.Translations
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

    override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor): IntentionPreviewInfo = IntentionPreviewInfo.EMPTY

    private fun removeTranslation(env: IEnvironment, progress: IProgress) {
        val singleStoreManager = Translations.createManager(Stream.of(store)).orElse(null) ?: return
        singleStoreManager.reload(env, progress) // in case the filesystem changed since the inspection was executed
        singleStoreManager.removeTranslations(Stream.of(key))
        singleStoreManager.flush(env, progress)
    }
}