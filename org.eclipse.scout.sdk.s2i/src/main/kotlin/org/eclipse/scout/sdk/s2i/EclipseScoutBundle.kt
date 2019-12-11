package org.eclipse.scout.sdk.s2i

import com.intellij.CommonBundle
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.util.Disposer
import org.eclipse.scout.sdk.s2i.classid.DuplicateClassIdInspection
import org.eclipse.scout.sdk.s2i.derived.DerivedResourceManager
import org.jetbrains.annotations.PropertyKey
import java.lang.ref.Reference
import java.lang.ref.SoftReference
import java.util.*
import java.util.concurrent.TimeUnit

class EclipseScoutBundle : StartupActivity, DumbAware {

    /**
     * Executed on [Project] open
     */
    override fun runActivity(project: Project) {
        val derivedResourceManager = derivedResourceManager(project)
        Disposer.register(project, derivedResourceManager)
        scheduleDuplicateClassIdInspectionIfEnabled(project, TimeUnit.MINUTES.toMillis(10))
    }

    companion object {

        private const val BUNDLE = "messages.EclipseScoutBundle"
        private var ourBundle: Reference<ResourceBundle>? = null

        fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String =
                CommonBundle.message(getBundle(), key, *params)

        fun derivedResourceManager(project: Project): DerivedResourceManager =
                ServiceManager.getService(project, DerivedResourceManager::class.java)

        fun scheduleDuplicateClassIdInspectionIfEnabled(project: Project, startupDelayMillis: Long) {
            DuplicateClassIdInspection.scheduleIfEnabled(project, startupDelayMillis, TimeUnit.MILLISECONDS)
        }

        private fun getBundle(): ResourceBundle {
            val cachedBundle = com.intellij.reference.SoftReference.dereference(ourBundle)
            if (cachedBundle != null) {
                return cachedBundle
            }

            val bundle = ResourceBundle.getBundle(BUNDLE)
            ourBundle = SoftReference(bundle)
            return bundle
        }
    }
}
