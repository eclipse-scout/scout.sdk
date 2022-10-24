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
package org.eclipse.scout.sdk.s2i.nls.editor

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ValidationInfo
import org.eclipse.scout.sdk.core.s.nls.ITranslationStore
import org.eclipse.scout.sdk.core.s.nls.Translation
import org.eclipse.scout.sdk.core.s.nls.TranslationValidator
import org.eclipse.scout.sdk.core.s.nls.manager.IStackedTranslation
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle

class TranslationNewDialog(project: Project, val store: ITranslationStore, manager: TranslationManager, initialKey: String? = null) : AbstractTranslationDialog(project, store.languages().toList(), manager, initialKey) {

    private var m_createdTranslation: IStackedTranslation? = null

    init {
        title = EclipseScoutBundle.message("create.new.translation.in.x", store.service().type().elementName())
    }

    override fun doSave(result: Translation) {
        m_createdTranslation = translationManager.setTranslationToStore(result, store)
    }

    override fun validateValues(): MutableList<ValidationInfo?> {
        val result = super.validateValues()
        result.add(validateKeyField())
        return result
    }

    private fun validateKeyField(): ValidationInfo? {
        val key = keyTextField().text ?: ""
        return toValidationInfo(TranslationValidator.validateKey(translationManager, store, key))
    }

    fun createdTranslation() = m_createdTranslation
}