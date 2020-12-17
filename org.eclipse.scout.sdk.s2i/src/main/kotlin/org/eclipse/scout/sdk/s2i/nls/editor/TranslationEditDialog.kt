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
package org.eclipse.scout.sdk.s2i.nls.editor

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ValidationInfo
import org.eclipse.scout.sdk.core.s.nls.ITranslationEntry
import org.eclipse.scout.sdk.core.s.nls.Language
import org.eclipse.scout.sdk.core.s.nls.Translation
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle

class TranslationEditDialog(project: Project, val translation: ITranslationEntry, stack: TranslationStoreStack, initialLanguageShown: Language? = null) : AbstractTranslationDialog(project, translation.store(), stack, translation.key(), initialLanguageShown) {

    init {
        title = EclipseScoutBundle.message("edit.translation.x", translation.key())
        translation.texts().forEach { languageTextField(it.key)?.text = it.value }
        keyTextField().isEnabled = false
    }

    override fun validateKeyField(): ValidationInfo? = null

    override fun doSave(result: Translation) {
        stack.updateTranslation(result).orElse(null)
    }
}