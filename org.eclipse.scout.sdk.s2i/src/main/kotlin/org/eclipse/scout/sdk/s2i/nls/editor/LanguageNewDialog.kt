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
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import org.eclipse.scout.sdk.core.s.nls.ITranslationStore
import org.eclipse.scout.sdk.core.s.nls.Language
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import java.util.*
import javax.swing.JComponent

class LanguageNewDialog(val project: Project, val store: ITranslationStore, val stack: TranslationStoreStack) : DialogWrapper(project, true, IdeModalityType.PROJECT) {

    private var m_comboBox: ComboBox<Language>? = null

    init {
        title = message("create.new.language")
        init()
        setResizable(false)
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
            stack.addNewLanguage(selectedItem, store)
        }
    }

    override fun getPreferredFocusedComponent() = languagesBox()
}