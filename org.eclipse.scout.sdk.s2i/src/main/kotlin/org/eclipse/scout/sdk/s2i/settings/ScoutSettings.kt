package org.eclipse.scout.sdk.s2i.settings

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.Disposable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.util.EventListenerList
import org.eclipse.scout.sdk.s2i.IdeaLogger
import java.util.*
import javax.swing.JComponent

open class ScoutSettings(private val project: Project) : SearchableConfigurable, Disposable {

    companion object Current {

        const val autoUpdateDerivedResourcesKey = "org.eclipse.scout.sdk.s2i.autoUpdateDerivedResources"
        const val autoCreateClassIdAnnotationsKey = "org.eclipse.scout.sdk.s2i.autoCreateClassIdAnnotations"
        const val logLevelKey = "org.eclipse.scout.sdk.s2i.logLevel"
        private val listeners = EventListenerList()

        fun logLevel(newValue: String) = PropertiesComponent.getInstance().setValue(logLevelKey, newValue)

        fun isLogLevelConfigured() = PropertiesComponent.getInstance().isValueSet(logLevelKey)

        fun logLevel(): String {
            return PropertiesComponent.getInstance().getValue(logLevelKey, IdeaLogger.LEVEL_WARNING)
        }

        fun isAutoUpdateDerivedResources(project: Project): Boolean {
            val store = PropertiesComponent.getInstance(project)
            return store.getBoolean(autoUpdateDerivedResourcesKey, true)
        }

        fun isAutoCreateClassIdAnnotations(project: Project): Boolean {
            val store = PropertiesComponent.getInstance(project)
            return store.getBoolean(autoCreateClassIdAnnotationsKey, false)
        }

        fun addListener(listener: SettingsChangedListener) {
            listeners.add(listener)
        }

        fun removeListener(listener: SettingsChangedListener) {
            listeners.remove(listener)
        }
    }

    private var m_ui: ScoutSettingsForm? = null

    override fun isModified(): Boolean {
        return !Objects.equals(logLevelInUi(), logLevel())
                || isAutoUpdateDerivedResourcesInUi() != isAutoUpdateDerivedResources(project)
                || isAutoCreateClassIdAnnotationsInUi() != isAutoCreateClassIdAnnotations(project)
    }

    override fun getId(): String {
        return "preferences.ScoutSettings"
    }

    override fun getDisplayName(): String {
        return "Scout"
    }

    override fun apply() {
        changeProperty(PropertiesComponent.getInstance(project), autoUpdateDerivedResourcesKey, isAutoUpdateDerivedResources(project).toString(), isAutoUpdateDerivedResourcesInUi().toString())
        changeProperty(PropertiesComponent.getInstance(project), autoCreateClassIdAnnotationsKey, isAutoCreateClassIdAnnotations(project).toString(), isAutoCreateClassIdAnnotationsInUi().toString())
        changeProperty(PropertiesComponent.getInstance(), logLevelKey, logLevel(), logLevelInUi())
    }

    private fun changeProperty(scope: PropertiesComponent, key: String, oldValue: String?, newValue: String?) {
        if (Objects.equals(oldValue, newValue)) {
            return
        }
        scope.setValue(key, newValue)
        notifyListeners(key, oldValue, newValue)
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

    override fun reset() {
        val ui = m_ui ?: return
        ui.autoUpdateDerivedResources.isSelected = isAutoUpdateDerivedResources(project)
        ui.autoCreateClassIdAnnotations.isSelected = isAutoCreateClassIdAnnotations(project)
        ui.logLevel.selectedItem = logLevel()
    }

    fun isAutoUpdateDerivedResourcesInUi(): Boolean {
        val ui = m_ui ?: return false
        return ui.autoUpdateDerivedResources.isSelected
    }

    fun isAutoCreateClassIdAnnotationsInUi(): Boolean {
        val ui = m_ui ?: return false
        return ui.autoCreateClassIdAnnotations.isSelected
    }

    fun logLevelInUi(): String? {
        val ui = m_ui ?: return null
        return ui.logLevel.selectedItem.toString()
    }

    override fun createComponent(): JComponent? {
        val ui = ScoutSettingsForm()
        m_ui = ui
        return ui.`$$$getRootComponent$$$`()
    }

    override fun disposeUIResources() {
        Disposer.dispose(this)
    }

    override fun dispose() {
        m_ui = null
    }
}
