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
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import org.eclipse.scout.sdk.core.s.nls.ITranslationStore
import org.eclipse.scout.sdk.core.s.nls.Language
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import java.util.*
import javax.swing.JComponent

class LanguageNewDialog(val project: Project, val store: ITranslationStore, val translationManager: TranslationManager) : DialogWrapper(project, true, IdeModalityType.IDE) {

    private var m_comboBox: ComboBox<Language>? = null

    init {
        title = message("create.new.language")
        init()
        isResizable = false
    }

    override fun createCenterPanel(): JComponent {
        val rootPanel = DialogPanel() // do not use applyCallbacks. the API is different in newer IJ versions

        val allLanguages = Locale.getAvailableLocales()
                .map { Language(it) }
                .filter { !store.containsLanguage(it) }
                .sorted()
                .toTypedArray()
        val comboBox = ComboBox(allLanguages)
        rootPanel.add(comboBox)
        m_comboBox = comboBox
        return rootPanel
    }

    fun languagesBox() = m_comboBox!!

    override fun doOKAction() {
        if (!okAction.isEnabled) {
            return
        }
        doOk()
        close(OK_EXIT_CODE)
    }

    private fun doOk() {
        val selectedItem = languagesBox().selectedItem
        if (selectedItem is Language && Strings.hasText(selectedItem.displayName())) {
            translationManager.addNewLanguage(selectedItem, store)
        }
    }

    override fun getPreferredFocusedComponent() = languagesBox()
}