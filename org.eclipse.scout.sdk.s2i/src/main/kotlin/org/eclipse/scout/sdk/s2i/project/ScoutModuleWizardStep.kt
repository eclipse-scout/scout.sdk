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
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message

open class ScoutModuleWizardStep(val wizardContext: WizardContext, val builder: ScoutModuleBuilder) : ModuleWizardStep() {

    private val m_propertyGraph = PropertyGraph(null, true)
    private val m_groupIdProperty = m_propertyGraph.graphProperty(::defaultGroupId)
    private val m_artifactIdProperty = m_propertyGraph.graphProperty(::defaultArtifactId)
    private val m_displayNameProperty = m_propertyGraph.graphProperty(::defaultDisplayName)

    private lateinit var m_useUiLanguageJava: JBRadioButton

    private val m_contentPanel by lazy {
        panel {
            titledRow(message("project.name")) {
                row(message("group.id")) {
                    textField(m_groupIdProperty, 32)
                        .withValidationOnApply { validateGroupId(it) }
                        .withValidationOnInput { validateGroupId(it) }
                        .focused()
                }
                row(message("artifact.id")) {
                    textField(m_artifactIdProperty, 32)
                        .withValidationOnApply { validateArtifactId(it) }
                        .withValidationOnInput { validateArtifactId(it) }
                }
                row(message("display.name")) {
                    textField(m_displayNameProperty, 32)
                        .withValidationOnApply { validateDisplayName(it) }
                        .withValidationOnInput { validateDisplayName(it) }
                }
            }
            titledRow(message("ui.lang")) {
                buttonGroup {
                    row { m_useUiLanguageJava = radioButton("Java", { true }, {}).component }
                    row { radioButton("JavaScript") }
                }
            }
        }.apply {
            registerValidators(wizardContext.disposable)
        }
    }

    protected open fun validateGroupId(groupIdField: JBTextField): ValidationInfo? = getMavenGroupIdErrorMessage(groupIdField.text)?.let { ValidationInfo(it, groupIdField) }

    protected open fun validateArtifactId(artifactIdField: JBTextField): ValidationInfo? {
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

    protected open fun isModuleNameUsed(name: String): Boolean {
        val modulePrefix = "$name."
        return existingModules().any { it.name.startsWith(modulePrefix) }
    }

    protected open fun existingModules(): Array<Module> = wizardContext.project?.let { ModuleManager.getInstance(it).modules } ?: Module.EMPTY_ARRAY

    protected open fun validateDisplayName(displayNameField: JBTextField): ValidationInfo? = getDisplayNameErrorMessage(displayNameField.text)?.let { ValidationInfo(it, displayNameField) }

    protected open fun defaultGroupId() = "org.eclipse.scout.apps"

    protected open fun defaultArtifactId(): String {
        val base = "helloscout"
        var candidate = base
        var suffix = 1
        while (isModuleNameUsed(candidate)) {
            candidate = base + suffix
            suffix++
        }
        return candidate
    }

    protected open fun defaultDisplayName() = "My Application"

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
    }
}