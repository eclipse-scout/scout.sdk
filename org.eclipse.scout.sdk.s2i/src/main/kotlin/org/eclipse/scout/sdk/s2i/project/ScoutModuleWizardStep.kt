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
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.observable.properties.GraphPropertyImpl.Companion.graphProperty
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.panel
import org.eclipse.scout.sdk.core.s.project.ScoutProjectNewHelper.*
import org.eclipse.scout.sdk.core.s.util.maven.IMavenConstants
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message

class ScoutModuleWizardStep(val wizardContext: WizardContext, val builder: ScoutModuleBuilder) : ModuleWizardStep() {

    private val m_propertyGraph = PropertyGraph(null, true)
    private val m_groupIdProperty = m_propertyGraph.graphProperty { "org.eclipse.scout.apps" }
    private val m_artifactIdProperty = m_propertyGraph.graphProperty(::defaultArtifactId)
    private val m_displayNameProperty = m_propertyGraph.graphProperty { "My Application" }
    private val m_versionProperty = m_propertyGraph.graphProperty { IMavenConstants.LATEST }
    private lateinit var m_useUiLanguageJava: JBRadioButton

    private val m_contentPanel by lazy {
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
            titledRow(message("scout.settings")) {
                row(message("ui.lang")) {
                    buttonGroup {
                        row { m_useUiLanguageJava = radioButton("Java", { true }, {}).component }
                        row { radioButton("JavaScript") }
                    }
                }
                row(message("scout.version")) {
                    textField(m_versionProperty, columns)
                }
            }
        }.apply {
            registerValidators(wizardContext.disposable)
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

    override fun validate() = m_contentPanel.validateCallbacks.asSequence()
        .mapNotNull { it() }
        .all { it.okEnabled }

    override fun getComponent() = m_contentPanel

    override fun updateDataModel() {
        wizardContext.projectBuilder = builder
        wizardContext.projectName = m_artifactIdProperty.get()

        builder.groupId = m_groupIdProperty.get()
        builder.artifactId = m_artifactIdProperty.get()
        builder.displayName = m_displayNameProperty.get()
        builder.useJavaUiLang = m_useUiLanguageJava.isSelected
        val version = m_versionProperty.get().trim()
        if (version.isNotEmpty()) {
            builder.version = version
        }
    }
}