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
import org.eclipse.scout.sdk.core.s.nls.Language
import org.eclipse.scout.sdk.core.s.nls.Translation
import org.eclipse.scout.sdk.core.s.nls.TranslationValidator.validateDefaultText
import org.eclipse.scout.sdk.core.s.nls.manager.IStackedTranslation
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle
import java.util.stream.Collectors.toList

class TranslationEditDialog(project: Project, val translation: IStackedTranslation, manager: TranslationManager, initialLanguageShown: Language? = null) :
    AbstractTranslationDialog(project, translation.languagesOfAllStores().collect(toList()), manager, translation.key(), initialLanguageShown) {

    init {
        title = EclipseScoutBundle.message("edit.translation.x", translation.key())
        translation.texts().forEach { languageTextField(it.key)?.text = it.value }
        keyTextField().isEnabled = false
    }

    override fun validateDefaultTextField(key: String, defaultLanguageText: String): ValidationInfo? {
        return toValidationInfo(validateDefaultText(defaultLanguageText, translation, null))
    }

    override fun isDialogChanged() = languageTextFields().any {
        val language = it.key
        val textFieldContent = it.value.text
        translation.text(language).orElse("") != Strings.notEmpty(textFieldContent).orElse("")
    }

    override fun doSave(result: Translation) {
        translationManager.setTranslation(result)
    }
}