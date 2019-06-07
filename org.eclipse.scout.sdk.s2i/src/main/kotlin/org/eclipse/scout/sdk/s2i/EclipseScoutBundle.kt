package org.eclipse.scout.sdk.s2i

import com.intellij.CommonBundle
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import org.eclipse.scout.sdk.s2i.classid.DuplicateClassIdInspection
import org.eclipse.scout.sdk.s2i.derived.DerivedResourceManager
import org.jetbrains.annotations.PropertyKey
import java.lang.ref.Reference
import java.lang.ref.SoftReference
import java.util.*
import java.util.concurrent.TimeUnit

class EclipseScoutBundle private constructor(private val project: Project) : ProjectComponent {

    override fun initComponent() = synchronized(this) {
        scheduleDuplicateClassIdInspectionIfEnabled(project, TimeUnit.MINUTES.toMillis(10))
    }

    companion object {

        private const val BUNDLE = "messages.EclipseScoutBundle"
        private var ourBundle: Reference<ResourceBundle>? = null

        fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String =
                CommonBundle.message(getBundle(), key, *params)

        fun derivedResourceManager(project: Project): DerivedResourceManager =
                project.getComponent(DerivedResourceManager::class.java)

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
