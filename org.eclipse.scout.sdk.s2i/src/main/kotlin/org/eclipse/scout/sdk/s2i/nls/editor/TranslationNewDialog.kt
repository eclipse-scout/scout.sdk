/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.nls.editor

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ValidationInfo
import org.apache.logging.log4j.util.Strings
import org.eclipse.scout.sdk.core.s.nls.ITranslationStore
import org.eclipse.scout.sdk.core.s.nls.Translation
import org.eclipse.scout.sdk.core.s.nls.TranslationValidator
import org.eclipse.scout.sdk.core.s.nls.TranslationValidator.validateDefaultText
import org.eclipse.scout.sdk.core.s.nls.manager.IStackedTranslation
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle
import java.util.stream.Collectors.toList

class TranslationNewDialog(project: Project, val store: ITranslationStore, manager: TranslationManager, initialKey: String? = null) : AbstractTranslationDialog(project, store.languages().collect(toList()), manager, initialKey) {

    private var m_createdTranslation: IStackedTranslation? = null

    init {
        title = EclipseScoutBundle.message("create.new.translation.in.x", store.service().type().elementName())
    }

    override fun validateDefaultTextField(key: String, defaultLanguageText: String): ValidationInfo? {
        return toValidationInfo(validateDefaultText(defaultLanguageText, key, translationManager, store))
    }

    override fun doSave(result: Translation) {
        m_createdTranslation = translationManager.setTranslationToStore(result, store)
    }

    override fun isDialogChanged(): Boolean {
        // dialog is changed in case any text is entered in key or a language field
        return Strings.isNotEmpty(keyTextField().text) || languageTextFields().values.any { Strings.isNotEmpty(it.text) }
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