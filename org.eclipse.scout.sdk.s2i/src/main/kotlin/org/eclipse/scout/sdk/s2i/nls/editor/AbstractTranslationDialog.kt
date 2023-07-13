/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.nls.editor

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.JBColor
import com.intellij.ui.components.*
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import org.eclipse.scout.sdk.core.s.nls.Language
import org.eclipse.scout.sdk.core.s.nls.Translation
import org.eclipse.scout.sdk.core.s.nls.TranslationValidator.*
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager
import org.eclipse.scout.sdk.core.util.CoreUtils
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.ui.IndexedFocusTraversalPolicy
import org.eclipse.scout.sdk.s2i.ui.TextFieldWithMaxLen
import java.awt.*
import java.awt.event.KeyEvent
import javax.swing.*
import javax.swing.event.DocumentEvent

@Suppress("LeakingThis")
abstract class AbstractTranslationDialog(val project: Project, val languages: Collection<Language>, val translationManager: TranslationManager, val initialKey: String? = null, val initialLanguageShown: Language? = null) : DialogWrapper(project, true, IdeModalityType.PROJECT) {

    private val m_dimensionKey = "scout.nls.translationDialog"
    private val m_languageTextFields = LinkedHashMap<Language, JBTextArea>()
    private var m_keyTextField: JBTextField? = null
    private var m_copyToClipboardField: JBCheckBox? = null
    private var m_tabPane: JBTabbedPane? = null
    private var m_errorStatusField: JBLabel? = null

    init {
        init()
    }

    override fun createCenterPanel(): JComponent {
        val rootPanel = DialogPanel(GridBagLayout()) // do not use applyCallbacks. the API is different in newer IJ versions
        rootPanel.preferredSize = Dimension(600, 300)

        // Key
        rootPanel.add(
            JBLabel(NlsTableModel.KEY_COLUMN_HEADER_NAME), GridBagConstraints(
                0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START,
                GridBagConstraints.NONE, JBUI.insets(4, 0, 0, 15), 0, 0
            )
        )
        val keyField = TextFieldWithMaxLen(maxLength = 200)
        keyField.isFocusable = true
        initialKey?.let { keyField.text = it }
        m_keyTextField = keyField
        rootPanel.add(
            keyField, GridBagConstraints(
                1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, JBUI.insets(4, 0, 0, 15), 0, 0
            )
        )

        // Copy to clipboard
        val copyToClipboardField = JBCheckBox(message("copy.key.to.clipboard"), false)
        copyToClipboardField.toolTipText = message("copy.key.to.clipboard.desc")
        copyToClipboardField.isFocusable = true
        m_copyToClipboardField = copyToClipboardField
        rootPanel.add(
            copyToClipboardField, GridBagConstraints(
                2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_END,
                GridBagConstraints.NONE, JBUI.insetsTop(4), 0, 0
            )
        )

        // Tab pane
        val tabPane = createTabPane()
        tabPane.isFocusable = true
        m_tabPane = tabPane
        rootPanel.add(
            tabPane, GridBagConstraints(
                0, 1, 3, 1, 1.0, 1.0, GridBagConstraints.PAGE_START,
                GridBagConstraints.BOTH, JBUI.insetsTop(10), 0, 0
            )
        )

        // validation label
        val statusLabel = JBLabel()
        statusLabel.border = null
        statusLabel.preferredSize = Dimension(600, 40)
        statusLabel.verticalAlignment = JLabel.TOP
        m_errorStatusField = statusLabel
        rootPanel.add(
            statusLabel, GridBagConstraints(
                0, 2, 3, 1, 1.0, 0.0, GridBagConstraints.LINE_START,
                GridBagConstraints.HORIZONTAL, JBUI.insets(5, 7, 0, 0), 0, 0
            )
        )

        isOKActionEnabled = false
        installValidation()
        if (initialKey == null) {
            // only generate key if not opened with an explicit one
            defaultLanguageTextField().document.addDocumentListener(KeyAutoGenerator())
        }

        rootPanel.isFocusTraversalPolicyProvider = true
        rootPanel.isFocusCycleRoot = true
        val focusPolicy = IndexedFocusTraversalPolicy()
        m_languageTextFields.values.forEach { focusPolicy.addComponent(it) }
        focusPolicy.addComponent(keyTextField())
        focusPolicy.addComponent(copyToClipboardField)
        focusPolicy.addComponent(tabPane)
        rootPanel.focusTraversalPolicy = focusPolicy

        if (initialLanguageShown != null && initialLanguageShown != Language.LANGUAGE_DEFAULT) {
            revealLanguage(initialLanguageShown)
        }

        return rootPanel
    }

    fun revealLanguage(language: Language) {
        val tabPane = m_tabPane ?: return
        val index = m_languageTextFields.keys.indexOf(language)
        if (index >= 0 && index < tabPane.tabCount) {
            tabPane.selectedIndex = index
        }
    }

    private fun createTabPane(): JBTabbedPane {
        val tabPane = JBTabbedPane(SwingConstants.TOP, JTabbedPane.WRAP_TAB_LAYOUT)
        languages.sorted().forEach {
            val txt = JBTextArea()
            txt.font = keyTextField().font
            txt.margin = JBUI.insets(5, 7, 5, 5)
            txt.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, setOf(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0)))
            txt.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, setOf(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK)))
            m_languageTextFields[it] = txt

            val panel = JBScrollPane(txt, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)
            panel.border = BorderFactory.createLineBorder(JBColor.border())

            tabPane.addTab(it.displayName(), panel)
        }
        return tabPane
    }

    private fun installValidation() {
        val triggerValidation = object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) {
                setErrorStatus(validateValues().filterNotNull())
            }
        }
        keyTextField()
                .takeIf { it.isEnabled }
                ?.document
                ?.addDocumentListener(triggerValidation)
        m_languageTextFields.values
                .filter { it.isEnabled }
                .forEach { it.document.addDocumentListener(triggerValidation) }
    }

    protected open fun validateValues(): MutableList<ValidationInfo?> {
        return mutableListOf(validateDefaultTextField())
    }

    private fun setErrorStatus(infos: List<ValidationInfo>) {
        val field = errorStatusField()
        field.text = ""
        isOKActionEnabled = infos.all { it.okEnabled }
        if (infos.isEmpty()) {
            return
        }

        val builder = StringBuilder("<html>")
        val iterator = infos.iterator()
        var info = iterator.next()
        builder.append(validationInfoToHtml(info))
        while (iterator.hasNext()) {
            info = iterator.next()
            builder.append("<br>").append(validationInfoToHtml(info))
        }
        builder.append("</html>")
        field.text = builder.toString()
    }

    private fun validationInfoToHtml(info: ValidationInfo): String {
        val color = htmlColorString(if (info.warning) MessageType.WARNING.borderColor else UIUtil.getErrorForeground())
        val message = Strings.escapeHtml(info.message)
        return "<font color=\"${color}\">$message</font>"
    }

    fun htmlColorString(color: Color): String {
        val red = Integer.toHexString(color.red)
        val green = Integer.toHexString(color.green)
        val blue = Integer.toHexString(color.blue)
        return "#" +
                (if (red.length == 1) "0$red" else red) +
                (if (green.length == 1) "0$green" else green) +
                if (blue.length == 1) "0$blue" else blue
    }

    protected open fun validateDefaultTextField(): ValidationInfo? {
        val defaultText = defaultLanguageTextField().text
        return toValidationInfo(validateDefaultText(defaultText, translationManager.translation(keyTextField().text).orElse(null)))
    }

    protected open fun toValidationInfo(errorCode: Int): ValidationInfo? {
        val info = when (errorCode) {
            OK -> null
            DEFAULT_TRANSLATION_MISSING_ERROR -> ValidationInfo(message("please.provide.text.for.lang.x", Language.LANGUAGE_DEFAULT.displayName()))
            KEY_EMPTY_ERROR -> ValidationInfo(message("please.specify.translation.key.with.desc"))
            KEY_ALREADY_EXISTS_ERROR -> ValidationInfo(message("key.already.exists"))
            KEY_OVERRIDES_OTHER_STORE_WARNING -> ValidationInfo(message("key.would.override.desc"))
            KEY_IS_OVERRIDDEN_BY_OTHER_STORE_WARNING -> ValidationInfo(message("key.would.be.overridden.desc"))
            KEY_OVERRIDES_AND_IS_OVERRIDDEN_WARNING -> ValidationInfo(message("key.overrides.and.is.overridden.desc"))
            TEXT_INHERITED_BECOMES_ACTIVE_IF_REMOVED_WARNING -> null
            else -> ValidationInfo(message("please.specify.translation.key.with.desc"))
        }
        if (info == null || isForbidden(errorCode)) {
            return info
        }
        return info.asWarning().withOKEnabled()
    }

    fun languageTextField(language: Language): JBTextArea? = m_languageTextFields[language]

    fun defaultLanguageTextField() = m_languageTextFields[Language.LANGUAGE_DEFAULT]!!

    fun keyTextField() = m_keyTextField!!

    fun errorStatusField() = m_errorStatusField!!

    protected open fun resultingTranslation(): Translation {
        val key = keyTextField().text
        val result = Translation(key)
        m_languageTextFields.entries
                .filter { Strings.hasText(it.value.text) }
                .forEach { result.putText(it.key, it.value.text) }
        return result
    }

    override fun doOKAction() {
        if (!okAction.isEnabled) {
            return
        }
        val translation = resultingTranslation()
        doSave(translation)
        if (m_copyToClipboardField?.isSelected == true) {
            CoreUtils.setTextToClipboard(translation.key())
        }
        close(OK_EXIT_CODE)
    }

    protected abstract fun doSave(result: Translation)

    override fun getPreferredFocusedComponent() = languageTextField(initialLanguageShown ?: Language.LANGUAGE_DEFAULT)

    override fun getDimensionServiceKey() = m_dimensionKey

    private inner class KeyAutoGenerator : DocumentAdapter() {
        private val m_defaultLangTextField = defaultLanguageTextField()
        private val m_keyTextField = keyTextField()
        private var m_lastDefaultLangText: String? = null

        override fun textChanged(e: DocumentEvent) {
            if (!m_keyTextField.isEnabled) {
                return
            }

            val defaultLangText = m_defaultLangTextField.text
            if (Strings.isBlank(defaultLangText)) {
                return
            }
            val oldKey = translationManager.generateNewKey(m_lastDefaultLangText)
            val curKey = Strings.notBlank(m_keyTextField.text).orElse("")
            if (curKey == oldKey) {
                m_keyTextField.text = translationManager.generateNewKey(defaultLangText)
            }
            m_lastDefaultLangText = defaultLangText
        }
    }
}