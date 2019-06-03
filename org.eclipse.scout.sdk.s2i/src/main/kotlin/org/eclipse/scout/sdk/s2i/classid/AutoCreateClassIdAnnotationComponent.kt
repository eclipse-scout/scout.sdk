package org.eclipse.scout.sdk.s2i.classid

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import org.eclipse.scout.sdk.core.s.classid.ClassIds
import org.eclipse.scout.sdk.s2i.settings.ScoutSettings
import org.eclipse.scout.sdk.s2i.settings.SettingsChangedListener

open class AutoCreateClassIdAnnotationComponent(private val project: Project) : ProjectComponent, SettingsChangedListener {

    override fun initComponent() = synchronized(this) {
        ScoutSettings.addListener(this)
    }

    override fun changed(key: String, oldVal: String?, newVal: String?) {
        if (ScoutSettings.autoCreateClassIdAnnotationsKey == key) {
            updateClassIdAutoGeneration()
        }
    }

    protected fun isAutoCreateClassIdAnnotations(): Boolean = ScoutSettings.isAutoCreateClassIdAnnotations(project)

    protected fun updateClassIdAutoGeneration() {
        ClassIds.setAutomaticallyCreateClassIdAnnotation(isAutoCreateClassIdAnnotations())
    }

    override fun disposeComponent() = synchronized(this) {
        ScoutSettings.removeListener(this)
    }
}
