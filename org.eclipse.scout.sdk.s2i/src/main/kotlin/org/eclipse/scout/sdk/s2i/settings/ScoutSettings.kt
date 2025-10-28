/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.settings

import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle
import org.eclipse.scout.sdk.s2i.settings.ScoutSettingsHelper.getTranslationLanguage
import org.eclipse.scout.sdk.s2i.settings.ScoutSettingsHelper.isAutoCreateClassIdAnnotations
import org.eclipse.scout.sdk.s2i.settings.ScoutSettingsHelper.isAutoUpdateDerivedResources
import org.eclipse.scout.sdk.s2i.settings.ScoutSettingsHelper.isAutoUpdateIdeSettings
import org.eclipse.scout.sdk.s2i.settings.ScoutSettingsHelper.setAutoCreateClassIdAnnotations
import org.eclipse.scout.sdk.s2i.settings.ScoutSettingsHelper.setAutoUpdateDerivedResources
import org.eclipse.scout.sdk.s2i.settings.ScoutSettingsHelper.setAutoUpdateIdeSettings
import org.eclipse.scout.sdk.s2i.settings.ScoutSettingsHelper.setTranslationLanguage
import javax.swing.JComponent

open class ScoutSettings(private val m_project: Project) : SearchableConfigurable {

    companion object {
        const val CONFIGURABLE_ID = "preferences.ScoutSettings"
        const val DISPLAY_NAME = "Scout"
        const val KEY_AUTO_UPDATE_DERIVED_RESOURCES = "${EclipseScoutBundle.ID}.autoUpdateDerivedResources"
        const val KEY_AUTO_CREATE_CLASS_ID = "${EclipseScoutBundle.ID}.autoCreateClassIdAnnotations"
        const val KEY_AUTO_UPDATE_IDE_SETTINGS = "${EclipseScoutBundle.ID}.autoUpdateIdeSettings"
        const val KEY_TRANSLATION_DEFAULT_LANG = "${EclipseScoutBundle.ID}.translationDefaultLanguage"
    }

    private val m_form: ScoutSettingsForm by lazy { ScoutSettingsForm() }

    private fun isAutoUpdateDerivedResourcesInUi() = m_form.isAutoUpdateDerivedResources

    private fun isAutoCreateClassIdAnnotationsInUi() = m_form.isAutoCreateClassId

    private fun isAutoUpdateIdeSettingsInUi() = m_form.isAutoUpdateIdeSettings

    private fun getTranslationLanguageInUi() = m_form.translationLanguage

    override fun isModified() =
        isAutoUpdateDerivedResourcesInUi() != isAutoUpdateDerivedResources(m_project)
                || isAutoCreateClassIdAnnotationsInUi() != isAutoCreateClassIdAnnotations(m_project)
                || isAutoUpdateIdeSettingsInUi() != isAutoUpdateIdeSettings(m_project)
                || getTranslationLanguageInUi() != getTranslationLanguage(m_project)

    override fun getId() = CONFIGURABLE_ID

    override fun getDisplayName() = DISPLAY_NAME

    override fun apply() {
        setAutoUpdateDerivedResources(m_project, isAutoUpdateDerivedResourcesInUi())
        setAutoCreateClassIdAnnotations(m_project, isAutoCreateClassIdAnnotationsInUi())
        setAutoUpdateIdeSettings(m_project, isAutoUpdateIdeSettingsInUi())
        setTranslationLanguage(m_project, getTranslationLanguageInUi())
    }

    override fun reset() {
        m_form.isAutoUpdateDerivedResources = isAutoUpdateDerivedResources(m_project)
        m_form.isAutoCreateClassId = isAutoCreateClassIdAnnotations(m_project)
        m_form.isAutoUpdateIdeSettings = isAutoUpdateIdeSettings(m_project)
        m_form.translationLanguage = getTranslationLanguage(m_project)
    }

    override fun createComponent(): JComponent? {
        reset()
        return m_form
    }
}
