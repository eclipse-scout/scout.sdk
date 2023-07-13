/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.settings

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.uiDesigner.core.Spacer
import com.intellij.util.ui.JBUI
import org.eclipse.scout.sdk.core.s.nls.Language
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle
import java.awt.Dimension
import java.util.*
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox

class ScoutSettingsForm : JBPanel<ScoutSettingsForm>() {

    private val m_htmlDisable = "html.disable"
    private val m_autoUpdateDerivedResources: JBCheckBox
    private val m_autoCreateClassIdAnnotations: JBCheckBox
    private val m_translationLanguageComboBox: JComboBox<Language>
    private val m_translationLanguageLabel: JBLabel

    init {
        putClientProperty(m_htmlDisable, true)

        layout = GridLayoutManager(4, 2, JBUI.emptyInsets(), -1, -1)

        m_autoUpdateDerivedResources = JBCheckBox()
        m_autoUpdateDerivedResources.text = EclipseScoutBundle.message("automatically.update.generated.classes")
        m_autoUpdateDerivedResources.putClientProperty(m_htmlDisable, true)

        m_autoCreateClassIdAnnotations = JBCheckBox()
        m_autoCreateClassIdAnnotations.text = EclipseScoutBundle.message("automatically.create.classid.annotation")
        m_autoCreateClassIdAnnotations.putClientProperty(m_htmlDisable, true)

        m_translationLanguageLabel = JBLabel()
        m_translationLanguageLabel.text = EclipseScoutBundle.message("translation.display.language")
        m_translationLanguageLabel.putClientProperty(m_htmlDisable, true)

        m_translationLanguageComboBox = ComboBox()
        m_translationLanguageComboBox.model = buildLanguageModel()
        m_translationLanguageComboBox.putClientProperty(m_htmlDisable, true)

        add(
                m_autoUpdateDerivedResources,
                GridConstraints(
                        0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED,
                        null, null, null, 0, false
                )
        )
        add(
                m_autoCreateClassIdAnnotations,
                GridConstraints(
                        1, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED,
                        null, null, null, 0, false
                )
        )
        add(
                m_translationLanguageLabel,
                GridConstraints(
                        2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                        null, Dimension(82, 16), null, 0, false
                )
        )
        add(
                m_translationLanguageComboBox,
                GridConstraints(
                        2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                        null, null, null, 0, false
                )
        )
        add(
                Spacer(),
                GridConstraints(
                        3, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK, GridConstraints.SIZEPOLICY_WANT_GROW,
                        null, null, null, 0, false
                )
        )
    }

    private fun buildLanguageModel(): DefaultComboBoxModel<Language> {
        val model = DefaultComboBoxModel<Language>()
        model.addElement(Language.LANGUAGE_DEFAULT)
        Locale.getAvailableLocales()
                .map { Language(it) }
                .filter { Strings.hasText(it.toString()) }
                .sorted()
                .forEach { model.addElement(it) }
        return model
    }

    var isAutoUpdateDerivedResources
        get() = m_autoUpdateDerivedResources.isSelected
        set(value) {
            m_autoUpdateDerivedResources.isSelected = value
        }

    var isAutoCreateClassId
        get() = m_autoCreateClassIdAnnotations.isSelected
        set(value) {
            m_autoCreateClassIdAnnotations.isSelected = value
        }
    var translationLanguage
        get() = m_translationLanguageComboBox.selectedItem as Language
        set(value) {
            m_translationLanguageComboBox.selectedItem = value
        }
}
