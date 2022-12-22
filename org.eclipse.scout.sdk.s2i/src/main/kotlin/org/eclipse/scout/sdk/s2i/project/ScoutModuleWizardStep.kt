/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i.project

import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.ComponentValidator
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBRadioButton
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.ui.JBUI
import org.eclipse.scout.sdk.core.apidef.ApiVersion
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.project.ScoutProjectNewHelper
import org.eclipse.scout.sdk.core.s.util.maven.IMavenConstants
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.ui.TextFieldWithMaxLen
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.util.function.Supplier
import javax.swing.BorderFactory
import javax.swing.ButtonGroup
import javax.swing.DefaultComboBoxModel
import javax.swing.JPanel

class ScoutModuleWizardStep(val wizardContext: WizardContext, val builder: ScoutModuleBuilder) : ModuleWizardStep() {

    private val m_availableVersions = ArrayList<String>()
    private var m_loading = true

    private val m_rootPanel: JPanel
    private val m_namePanel: JPanel
    private val m_settingsPanel: JPanel
    private val m_groupIdTextField = TextFieldWithMaxLen(text = "org.eclipse.scout.apps", maxLength = 200)
    private val m_artifactIdTextField = TextFieldWithMaxLen(text = defaultArtifactId(), maxLength = 200)
    private val m_displayNameTextField = TextFieldWithMaxLen(text = "My Application", maxLength = 200)
    private val m_javaButton = JBRadioButton("Java", false)
    private val m_javaScriptButton = JBRadioButton("TypeScript / JavaScript", !m_javaButton.isSelected)
    private val m_versionsComboBox = ComboBox(VersionComboBoxModel())
    private val m_loadingIcon = JBLabel(AnimatedIcon.Default.INSTANCE)
    private val m_showPreviewVersionsCheckBox = JBCheckBox(message("also.show.preview.versions"), false)
    private val m_labelInsets = Insets(4, 10, 0, 15)
    private val m_fieldInsets = Insets(4, 0, 0, 15)

    init {
        m_namePanel = createNameGroup()
        m_settingsPanel = createSettingsGroup()
        m_rootPanel = createRootPanel()

        resetVersionsToDefault()
        updateVersionsAsync()
    }

    private fun createNameGroup(): JPanel {
        val namePanel = JPanel(GridBagLayout())

        ComponentValidator(wizardContext.disposable)
            .withValidator(Supplier { validateGroupId() })
            .andRegisterOnDocumentListener(m_groupIdTextField)
            .andStartOnFocusLost()
            .installOn(m_groupIdTextField)

        ComponentValidator(wizardContext.disposable)
            .withValidator(Supplier { validateArtifactId() })
            .andRegisterOnDocumentListener(m_artifactIdTextField)
            .andStartOnFocusLost()
            .installOn(m_artifactIdTextField)

        ComponentValidator(wizardContext.disposable)
            .withValidator(Supplier { validateDisplayName() })
            .andRegisterOnDocumentListener(m_displayNameTextField)
            .andStartOnFocusLost()
            .installOn(m_displayNameTextField)

        namePanel.border = BorderFactory.createTitledBorder(JBUI.Borders.customLineTop(JBUI.CurrentTheme.CustomFrameDecorations.separatorForeground()), message("project.name"))
        namePanel.add(
            JBLabel(message("group.id")), GridBagConstraints(
                0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, m_labelInsets, 50, 0
            )
        )
        namePanel.add(
            m_groupIdTextField, GridBagConstraints(
                1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_END, GridBagConstraints.HORIZONTAL, m_fieldInsets, 0, 0
            )
        )
        namePanel.add(
            JBLabel(message("artifact.id")), GridBagConstraints(
                0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, m_labelInsets, 50, 0
            )
        )
        namePanel.add(
            m_artifactIdTextField, GridBagConstraints(
                1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_END, GridBagConstraints.HORIZONTAL, m_fieldInsets, 0, 0
            )
        )
        namePanel.add(
            JBLabel(message("display.name")), GridBagConstraints(
                0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, m_labelInsets, 50, 0
            )
        )
        namePanel.add(
            m_displayNameTextField, GridBagConstraints(
                1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_END, GridBagConstraints.HORIZONTAL, m_fieldInsets, 0, 0
            )
        )
        return namePanel
    }

    private fun createSettingsGroup(): JPanel {
        val settingsPanel = JPanel(GridBagLayout())

        val versionValidator = ComponentValidator(wizardContext.disposable)
            .withValidator(Supplier { validateVersion() })
            .installOn(m_versionsComboBox)
        m_versionsComboBox.isEditable = true
        m_versionsComboBox.putClientProperty("html.disable", true)
        m_versionsComboBox.addActionListener { versionValidator.revalidate() }

        settingsPanel.border = BorderFactory.createTitledBorder(JBUI.Borders.customLineTop(JBUI.CurrentTheme.CustomFrameDecorations.separatorForeground()), message("project.settings"))
        settingsPanel.add(
            JBLabel(message("ui.lang")), GridBagConstraints(
                0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, Insets(9, 10, 0, 0), 11, 0
            )
        )
        val uiLangRadioButtonGroup = ButtonGroup()
        uiLangRadioButtonGroup.add(m_javaButton)
        uiLangRadioButtonGroup.add(m_javaScriptButton)
        m_javaButton.addActionListener { updateVersionsAsync() }
        m_javaScriptButton.addActionListener { updateVersionsAsync() }
        m_showPreviewVersionsCheckBox.addActionListener { updateVersionsAsync() }
        settingsPanel.add(
            m_javaButton, GridBagConstraints(
                1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_END, GridBagConstraints.HORIZONTAL, Insets(9, 0, 0, 15), 0, 0
            )
        )
        settingsPanel.add(
            m_javaScriptButton, GridBagConstraints(
                1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_END, GridBagConstraints.HORIZONTAL, m_fieldInsets, 0, 0
            )
        )
        settingsPanel.add(
            JBLabel(message("scout.version")), GridBagConstraints(
                0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, Insets(20, 10, 0, 15), 50, 0
            )
        )
        settingsPanel.add(
            m_versionsComboBox, GridBagConstraints(
                1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, Insets(20, 0, 0, 5), 0, 0
            )
        )
        settingsPanel.add(
            m_loadingIcon, GridBagConstraints(
                2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_END, GridBagConstraints.HORIZONTAL, Insets(20, 0, 0, 17), 0, 0
            )
        )
        settingsPanel.add(
            m_showPreviewVersionsCheckBox, GridBagConstraints(
                1, 3, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_END, GridBagConstraints.HORIZONTAL, m_fieldInsets, 0, 0
            )
        )
        settingsPanel.add(
            JBLabel(message("use.newest.recommendation")), GridBagConstraints(
                1, 4, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_END, GridBagConstraints.HORIZONTAL, Insets(4, 2, 0, 15), 0, 0
            )
        )
        return settingsPanel
    }

    private fun createRootPanel(): JPanel {
        val rootPanel = JPanel(GridBagLayout())
        rootPanel.add(
            m_namePanel, GridBagConstraints(
                0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_START,
                GridBagConstraints.HORIZONTAL, Insets(20, 8, 30, 8), 0, 0
            )
        )
        rootPanel.add(
            m_settingsPanel, GridBagConstraints(
                0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_START,
                GridBagConstraints.HORIZONTAL, Insets(0, 8, 30, 8), 0, 0
            )
        )
        rootPanel.add(
            JPanel(), GridBagConstraints(
                0, 2, 1, 1, 1.0, 1.0, GridBagConstraints.LINE_START,
                GridBagConstraints.HORIZONTAL, Insets(0, 8, 30, 8), 0, 0
            )
        )
        return rootPanel
    }

    private fun validateGroupId() = ScoutProjectNewHelper.getMavenGroupIdErrorMessage(m_groupIdTextField.text)
        ?.let { ValidationInfo(it, m_groupIdTextField) }

    private fun validateArtifactId(): ValidationInfo? {
        val artifactId = m_artifactIdTextField.text
        val idErrorMessage = ScoutProjectNewHelper.getMavenArtifactIdErrorMessage(artifactId)
        if (idErrorMessage != null) {
            return ValidationInfo(idErrorMessage, m_artifactIdTextField)
        }

        if (isModuleNameUsed(artifactId)) {
            return ValidationInfo(message("module.already.exists"), m_artifactIdTextField)
        }
        return null
    }

    private fun validateDisplayName() = ScoutProjectNewHelper.getDisplayNameErrorMessage(m_displayNameTextField.text)
        ?.let { ValidationInfo(it, m_displayNameTextField) }

    private fun validateVersion(): ValidationInfo? {
        val selectedValue = m_versionsComboBox.selectedItem as String?
        if (IMavenConstants.LATEST == selectedValue) {
            return null
        }
        if (ApiVersion.parse(selectedValue).isEmpty) {
            return ValidationInfo(message("invalid.scout.version"), m_versionsComboBox)
        }
        return null
    }

    override fun validate() = !m_loading
            && validateGroupId() == null
            && validateArtifactId() == null
            && validateDisplayName() == null
            && validateVersion() == null

    private fun updateVersionsAsync() {
        setLoading(true)
        AppExecutorUtil.getAppScheduledExecutorService().submit {
            try {
                val supportedVersions = ScoutProjectNewHelper.getSupportedArchetypeVersions(m_javaButton.isSelected, m_showPreviewVersionsCheckBox.isSelected)
                if (supportedVersions.isEmpty()) {
                    resetVersionsToDefault()
                } else {
                    setAvailableVersions(supportedVersions)
                }
            } catch (e: Exception) {
                SdkLog.warning("Error fetching available Scout versions from Maven central.", e)
                resetVersionsToDefault()
            } finally {
                ApplicationManager.getApplication().invokeLater({ setLoading(false) }, ModalityState.any())
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        m_loading = loading
        m_javaButton.isEnabled = !loading
        m_javaScriptButton.isEnabled = !loading
        m_versionsComboBox.isEnabled = !loading
        m_showPreviewVersionsCheckBox.isEnabled = !loading
        m_loadingIcon.isVisible = loading
    }

    private fun resetVersionsToDefault() {
        setAvailableVersions(listOf(IMavenConstants.LATEST))
    }

    private fun setAvailableVersions(versions: List<String>) {
        m_availableVersions.clear()
        m_availableVersions.addAll(versions)
        val cboModel = m_versionsComboBox.model as? VersionComboBoxModel
        cboModel?.setElements(versions)
    }

    private inner class VersionComboBoxModel : DefaultComboBoxModel<String>() {
        init {
            setElements(m_availableVersions)
        }

        fun setElements(elements: Collection<String>) = ApplicationManager.getApplication().invokeLater({
            val selected = selectedItem
            removeAllElements()
            addAll(elements)
            selectedItem = if (elements.contains(selected)) selected else elements.firstOrNull()
        }, ModalityState.any())
    }

    private fun isModuleNameUsed(name: String): Boolean {
        val modulePrefix = "$name."
        return existingModules().any { it.name.startsWith(modulePrefix) }
    }

    private fun existingModules(): Array<Module> = wizardContext.project?.let { ModuleManager.getInstance(it).modules } ?: Module.EMPTY_ARRAY

    private fun defaultArtifactId(): String {
        val base = "helloscout"
        var candidate = base
        var suffix = 1
        while (isModuleNameUsed(candidate)) {
            candidate = base + suffix
            suffix++
        }
        return candidate
    }

    override fun getComponent() = m_rootPanel

    override fun updateDataModel() {
        wizardContext.projectBuilder = builder
        wizardContext.projectName = m_artifactIdTextField.text

        builder.groupId = m_groupIdTextField.text
        builder.artifactId = m_artifactIdTextField.text
        builder.displayName = m_displayNameTextField.text
        builder.useJavaUiLang = m_javaButton.isSelected
        val version = m_versionsComboBox.selectedItem?.toString()
        if (version?.isNotEmpty() == true) {
            builder.scoutVersion = version
        }
    }
}
