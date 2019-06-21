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

        const val KEY_AUTO_UPDATE_DERIVED_RESOURCES = "org.eclipse.scout.sdk.s2i.autoUpdateDerivedResources"
        const val KEY_AUTO_CREATE_CLASS_ID = "org.eclipse.scout.sdk.s2i.autoCreateClassIdAnnotations"
        const val KEY_LOG_LEVEL = "org.eclipse.scout.sdk.s2i.logLevel"

        private val listeners = EventListenerList()

        fun getLogLevel(): IdeaLogger.LogLevel {
            return IdeaLogger.LogLevel.parse(PropertiesComponent.getInstance().getValue(KEY_LOG_LEVEL))
        }

        fun isLogLevelSet() = PropertiesComponent.getInstance().isValueSet(KEY_LOG_LEVEL)

        fun setLogLevel(newValue: IdeaLogger.LogLevel) = changeProperty(PropertiesComponent.getInstance(), KEY_LOG_LEVEL, getLogLevel().toString(), newValue.toString())

        fun isAutoUpdateDerivedResources(project: Project): Boolean {
            val store = PropertiesComponent.getInstance(project)
            return store.getBoolean(KEY_AUTO_UPDATE_DERIVED_RESOURCES, true)
        }

        @Suppress("unused")
        fun setAutoUpdateDerivedResources(project: Project, newValue: Boolean) =
                changeProperty(PropertiesComponent.getInstance(project), KEY_AUTO_UPDATE_DERIVED_RESOURCES, isAutoUpdateDerivedResources(project).toString(), newValue.toString())

        fun isAutoCreateClassIdAnnotations(project: Project): Boolean {
            val store = PropertiesComponent.getInstance(project)
            return store.getBoolean(KEY_AUTO_CREATE_CLASS_ID, false)
        }

        fun setAutoCreateClassIdAnnotations(project: Project, newValue: Boolean) =
                changeProperty(PropertiesComponent.getInstance(project), KEY_AUTO_CREATE_CLASS_ID, isAutoCreateClassIdAnnotations(project).toString(), newValue.toString())


        fun addListener(listener: SettingsChangedListener) {
            listeners.add(listener)
        }

        fun removeListener(listener: SettingsChangedListener) {
            listeners.remove(listener)
        }

        private fun changeProperty(scope: PropertiesComponent, key: String, oldValue: String?, newValue: String?): Boolean {
            if (Objects.equals(oldValue, newValue)) {
                return false
            }
            scope.setValue(key, newValue)
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

    private var m_form: ScoutSettingsForm? = null

    private fun isAutoUpdateDerivedResourcesInUi() = m_form?.isAutoUpdateDerivedResources ?: false

    private fun isAutoCreateClassIdAnnotationsInUi() = m_form?.isAutoCreateClassId ?: false

    private fun logLevelInUi() = m_form?.logLevel

    override fun isModified(): Boolean {
        return !Objects.equals(logLevelInUi(), getLogLevel())
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
        val projectSettings = PropertiesComponent.getInstance(project)
        changeProperty(
                projectSettings, KEY_AUTO_UPDATE_DERIVED_RESOURCES,
                isAutoUpdateDerivedResources(project).toString(),
                isAutoUpdateDerivedResourcesInUi().toString()
        )
        changeProperty(
                projectSettings, KEY_AUTO_CREATE_CLASS_ID,
                isAutoCreateClassIdAnnotations(project).toString(),
                isAutoCreateClassIdAnnotationsInUi().toString()
        )
        changeProperty(
                PropertiesComponent.getInstance(), KEY_LOG_LEVEL,
                getLogLevel().toString(),
                logLevelInUi().toString()
        )
    }

    override fun reset() {
        m_form?.isAutoUpdateDerivedResources = isAutoUpdateDerivedResources(project)
        m_form?.isAutoCreateClassId = isAutoCreateClassIdAnnotations(project)
        m_form?.logLevel = getLogLevel()
    }

    override fun createComponent(): JComponent? {
        m_form = ScoutSettingsForm()
        reset()
        return m_form
    }

    override fun disposeUIResources() {
        Disposer.dispose(this)
    }

    override fun dispose() {
        m_form = null
    }
}
