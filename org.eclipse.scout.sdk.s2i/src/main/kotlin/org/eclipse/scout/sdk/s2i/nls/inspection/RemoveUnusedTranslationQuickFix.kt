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
package org.eclipse.scout.sdk.s2i.nls.inspection

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import org.eclipse.scout.sdk.core.s.nls.ITranslationStore
import org.eclipse.scout.sdk.core.s.nls.TranslationStores
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.callInIdeaEnvironment
import java.util.stream.Stream

class RemoveUnusedTranslationQuickFix(val key: String, val store: ITranslationStore) : LocalQuickFix {

    val quickFixName = message("remove.unused.translation")

    override fun getFamilyName(): String = quickFixName

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        callInIdeaEnvironment(project, quickFixName) { env, progress ->
            val singleStoreStack = TranslationStores.createStack(Stream.of(store)).orElse(null) ?: return@callInIdeaEnvironment
            singleStoreStack.reload(progress) // in case the filesystem changed since the inspection was executed
            singleStoreStack.removeTranslations(Stream.of(key))
            singleStoreStack.flush(env, progress)
        }
    }
}