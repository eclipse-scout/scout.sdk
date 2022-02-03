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
import com.intellij.openapi.observable.properties.GraphProperty
import com.intellij.openapi.observable.properties.GraphPropertyImpl.Companion.graphProperty
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.ComponentPredicate
import com.intellij.ui.layout.panel
import com.intellij.util.concurrency.AppExecutorUtil
import org.eclipse.scout.sdk.core.apidef.ApiVersion
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.project.ScoutProjectNewHelper.*
import org.eclipse.scout.sdk.core.s.util.maven.IMavenConstants
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import java.awt.Dimension
import javax.swing.DefaultComboBoxModel

class ScoutModuleWizardStep(val wizardContext: WizardContext, val builder: ScoutModuleBuilder) : ModuleWizardStep() {

    private val m_propertyGraph = PropertyGraph(null, true)
    private val m_groupIdProperty = m_propertyGraph.graphProperty { "org.eclipse.scout.apps" }
    private val m_artifactIdProperty = m_propertyGraph.graphProperty(::defaultArtifactId)
    private val m_displayNameProperty = m_propertyGraph.graphProperty { "My Application" }
    private val m_versionProperty = m_propertyGraph.graphProperty { IMavenConstants.LATEST }
    private val m_showPreviewReleases = m_propertyGraph.graphProperty { false }
    private val m_useJavaAsUiLang = m_propertyGraph.graphProperty { true }
    private val m_loading = m_propertyGraph.graphProperty { true }
    private val m_availableVersions = ArrayList<String>()
    private lateinit var m_versionComboBox: ComboBox<String>

    private val m_contentPanelDelegate = lazy {
        panel {
            val columns = 32
            titledRow(message("project.name")) {
                row(message("group.id")) {
                    textField(m_groupIdProperty, columns)
                        .withValidationOnApply { validateGroupId(it) }
                        .withValidationOnInput { validateGroupId(it) }
                        .focused()
                }
                row(message("artifact.id")) {
                    textField(m_artifactIdProperty, columns)
                        .withValidationOnApply { validateArtifactId(it) }
                        .withValidationOnInput { validateArtifactId(it) }
                }
                row(message("display.name")) {
                    textField(m_displayNameProperty, columns)
                        .withValidationOnApply { validateDisplayName(it) }
                        .withValidationOnInput { validateDisplayName(it) }
                }
            }
            titledRow(message("ui.lang")) {
                buttonGroup {
                    row("") {
                        val javaButton = radioButton("Java")
                            .enableIf(m_loading.hasValue(false))
                            .component
                        javaButton.isSelected = m_useJavaAsUiLang.get()
                        javaButton.addActionListener { onUiLanguageChanged(javaButton.isSelected) }
                    }
                    row("") {
                        val javaScriptButton = radioButton("JavaScript")
                            .enableIf(m_loading.hasValue(false))
                            .component
                        javaScriptButton.isSelected = !m_useJavaAsUiLang.get()
                        javaScriptButton.addActionListener { onUiLanguageChanged(!javaScriptButton.isSelected) }
                    }
                }
            }
            titledRow(message("scout.version")) {
                row("") {
                    m_versionComboBox = comboBox(VersionComboBoxModel(), m_versionProperty)
                        .enableIf(m_loading.hasValue(false))
                        .withValidationOnInput { validateVersion(it) }
                        .withValidationOnApply { validateVersion(it) }
                        .component
                    m_versionComboBox.isEditable = true
                    val preferredWidth = m_versionComboBox.getFontMetrics(m_versionComboBox.font).charWidth('m') * columns + 6
                    m_versionComboBox.preferredSize = Dimension(preferredWidth, m_versionComboBox.preferredSize.height)

                    val iconLabel = JBLabel(AnimatedIcon.Default.INSTANCE)
                    iconLabel().visibleIf(m_loading.hasValue(true))
                }
                row("") {
                    checkBox(message("also.show.preview.versions"), m_showPreviewReleases)
                        .enableIf(m_loading.hasValue(false))
                        .comment(message("use.newest.recommendation"))
                        .component
                        .addActionListener { updateVersionsAsync() }
                }
            }
        }.apply {
            registerValidators(wizardContext.disposable)
        }
    }

    init {
        resetVersionsToDefault()
        updateVersionsAsync()
    }

    private fun <T> GraphProperty<T>.hasValue(expected: Boolean): ComponentPredicate {
        return object : ComponentPredicate() {
            override fun addListener(listener: (Boolean) -> Unit) {
                afterChange { listener(invoke()) }
            }

            override fun invoke() = expected == get()
        }
    }

    private fun onUiLanguageChanged(useJavaUiLang: Boolean) {
        if (m_useJavaAsUiLang.get() == useJavaUiLang) return
        m_useJavaAsUiLang.set(useJavaUiLang)
        updateVersionsAsync()
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

    private fun updateVersionsAsync() {
        m_loading.set(true)
        AppExecutorUtil.getAppScheduledExecutorService().submit {
            try {
                val supportedVersions = getSupportedArchetypeVersions(m_useJavaAsUiLang.get(), m_showPreviewReleases.get())
                if (supportedVersions.isEmpty()) {
                    resetVersionsToDefault()
                } else {
                    setAvailableVersions(supportedVersions)
                }
            } catch (e: Exception) {
                SdkLog.warning("Error fetching available Scout versions from Maven central.", e)
                resetVersionsToDefault()
            } finally {
                m_loading.set(false)
            }
        }
    }

    private fun resetVersionsToDefault() {
        setAvailableVersions(listOf(IMavenConstants.LATEST))
    }

    private fun setAvailableVersions(versions: List<String>) {
        m_availableVersions.clear()
        m_availableVersions.addAll(versions)
        if (m_contentPanelDelegate.isInitialized()) {
            val cboModel = m_versionComboBox.model as? VersionComboBoxModel
            cboModel?.setElements(versions)
        }
    }

    private fun validateGroupId(groupIdField: JBTextField): ValidationInfo? = getMavenGroupIdErrorMessage(groupIdField.text)?.let { ValidationInfo(it, groupIdField) }

    private fun validateArtifactId(artifactIdField: JBTextField): ValidationInfo? {
        val artifactId = artifactIdField.text
        val idErrorMessage = getMavenArtifactIdErrorMessage(artifactId)
        if (idErrorMessage != null) {
            return ValidationInfo(idErrorMessage, artifactIdField)
        }

        if (isModuleNameUsed(artifactId)) {
            return ValidationInfo(message("module.already.exists"), artifactIdField)
        }
        return null
    }

    private fun isModuleNameUsed(name: String): Boolean {
        val modulePrefix = "$name."
        return existingModules().any { it.name.startsWith(modulePrefix) }
    }

    private fun existingModules(): Array<Module> = wizardContext.project?.let { ModuleManager.getInstance(it).modules } ?: Module.EMPTY_ARRAY

    private fun validateDisplayName(displayNameField: JBTextField) = getDisplayNameErrorMessage(displayNameField.text)?.let {
        ValidationInfo(it, displayNameField)
    }

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

    private fun validateVersion(comboBox: ComboBox<String>): ValidationInfo? {
        val selectedValue = comboBox.selectedItem as String?
        if (IMavenConstants.LATEST == selectedValue) {
            return null
        }
        if (ApiVersion.parse(selectedValue).isEmpty) {
            return ValidationInfo(message("invalid.scout.version"), comboBox)
        }
        return null
    }

    override fun validate() = !m_loading.get() && m_contentPanelDelegate.value.validateCallbacks.asSequence()
        .mapNotNull { it() }
        .all { it.okEnabled }

    override fun getComponent() = m_contentPanelDelegate.value

    override fun updateDataModel() {
        wizardContext.projectBuilder = builder
        wizardContext.projectName = m_artifactIdProperty.get()

        builder.groupId = m_groupIdProperty.get()
        builder.artifactId = m_artifactIdProperty.get()
        builder.displayName = m_displayNameProperty.get()
        builder.useJavaUiLang = m_useJavaAsUiLang.get()
        val version = m_versionProperty.get().trim()
        if (version.isNotEmpty()) {
            builder.scoutVersion = version
        }
    }
}