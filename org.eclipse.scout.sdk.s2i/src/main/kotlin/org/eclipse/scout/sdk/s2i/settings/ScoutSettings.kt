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
package org.eclipse.scout.sdk.s2i.settings

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.nls.Language
import org.eclipse.scout.sdk.core.util.EventListenerList
import org.eclipse.scout.sdk.core.util.Strings
import java.util.*
import javax.swing.JComponent

open class ScoutSettings(private val project: Project) : SearchableConfigurable {

    companion object Current {

        const val CONFIGURABLE_ID = "preferences.ScoutSettings"
        const val DISPLAY_NAME = "Scout"
        const val KEY_AUTO_UPDATE_DERIVED_RESOURCES = "org.eclipse.scout.sdk.s2i.autoUpdateDerivedResources"
        const val KEY_AUTO_CREATE_CLASS_ID = "org.eclipse.scout.sdk.s2i.autoCreateClassIdAnnotations"
        const val KEY_TRANSLATION_DEFAULT_LANG = "org.eclipse.scout.sdk.s2i.translationDefaultLanguage"

        private val listeners = EventListenerList()

        fun getCodeFoldingSettings() = ScoutCodeFoldingSettings.getInstance()

        fun isAutoUpdateDerivedResources(project: Project) =
                projectSettings(project).getBoolean(KEY_AUTO_UPDATE_DERIVED_RESOURCES, true)

        fun setAutoUpdateDerivedResources(project: Project, newValue: Boolean) =
                changeProperty(projectSettings(project), KEY_AUTO_UPDATE_DERIVED_RESOURCES, isAutoUpdateDerivedResources(project).toString(), newValue.toString())

        fun isAutoCreateClassIdAnnotations(project: Project) =
                projectSettings(project).getBoolean(KEY_AUTO_CREATE_CLASS_ID, false)

        fun setAutoCreateClassIdAnnotations(project: Project, newValue: Boolean) =
                changeProperty(projectSettings(project), KEY_AUTO_CREATE_CLASS_ID, isAutoCreateClassIdAnnotations(project).toString(), newValue.toString())

        fun getTranslationLanguage(project: Project): Language {
            val raw = projectSettings(project).getValue(KEY_TRANSLATION_DEFAULT_LANG)
            return Strings.notBlank(raw)
                    .flatMap(Language::parse)
                    .orElseGet { Language(Locale.getDefault(Locale.Category.FORMAT)) }
        }

        fun setTranslationLanguage(project: Project, language: Language?) {
            val name = language?.locale().toString()
            val oldName = getTranslationLanguage(project).locale().toString()
            changeProperty(projectSettings(project), KEY_TRANSLATION_DEFAULT_LANG, oldName, name)
        }

        protected fun projectSettings(project: Project): PropertiesComponent = PropertiesComponent.getInstance(project)

        fun addListener(listener: SettingsChangedListener) {
            listeners.add(listener)
        }

        fun removeListener(listener: SettingsChangedListener) {
            listeners.remove(listener)
        }

        private fun changeProperty(store: PropertiesComponent, key: String, oldValue: String?, newValue: String?): Boolean {
            if (Objects.equals(oldValue, newValue)) {
                return false
            }
            store.setValue(key, newValue)
            notifyListeners(key, oldValue, newValue)
            return true
        }

        private fun notifyListeners(key: String, oldValue: String?, newValue: String?) {
            for (listener in listeners.get(SettingsChangedListener::class.java)) {
                try {
                    listener.changed(key, oldValue, newValue)
                } catch (e: RuntimeException) {
                    SdkLog.warning("Error in settings listener {}", listener::class.java, e)
                }
            }
        }
    }

    private val m_form: ScoutSettingsForm by lazy { ScoutSettingsForm() }

    private fun isAutoUpdateDerivedResourcesInUi() = m_form.isAutoUpdateDerivedResources

    private fun isAutoCreateClassIdAnnotationsInUi() = m_form.isAutoCreateClassId

    private fun getTranslationLanguageInUi() = m_form.translationLanguage

    override fun isModified() =
            isAutoUpdateDerivedResourcesInUi() != isAutoUpdateDerivedResources(project)
                    || isAutoCreateClassIdAnnotationsInUi() != isAutoCreateClassIdAnnotations(project)
                    || getTranslationLanguageInUi() != getTranslationLanguage(project)

    override fun getId() = CONFIGURABLE_ID

    override fun getDisplayName() = DISPLAY_NAME

    override fun apply() {
        setAutoUpdateDerivedResources(project, isAutoUpdateDerivedResourcesInUi())
        setAutoCreateClassIdAnnotations(project, isAutoCreateClassIdAnnotationsInUi())
        setTranslationLanguage(project, getTranslationLanguageInUi())
    }

    override fun reset() {
        m_form.isAutoUpdateDerivedResources = isAutoUpdateDerivedResources(project)
        m_form.isAutoCreateClassId = isAutoCreateClassIdAnnotations(project)
        m_form.translationLanguage = getTranslationLanguage(project)
    }

    override fun createComponent(): JComponent? {
        reset()
        return m_form
    }
}
