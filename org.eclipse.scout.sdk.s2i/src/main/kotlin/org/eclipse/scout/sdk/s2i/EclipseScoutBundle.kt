/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i

import com.intellij.BundleBase
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import org.eclipse.scout.sdk.s2i.classid.ClassIdCache
import org.eclipse.scout.sdk.s2i.derived.DerivedResourceManager
import org.jetbrains.annotations.PropertyKey
import java.lang.ref.Reference
import java.lang.ref.SoftReference
import java.util.*

class EclipseScoutBundle : StartupActivity, DumbAware {

    /**
     * Executed on [Project] open
     */
    override fun runActivity(project: Project) {
        derivedResourceManager(project).start() // it will dispose itself
    }

    companion object {

        const val RESOURCE_BUNDLE = "messages.EclipseScoutBundle"
        private var ourBundle: Reference<ResourceBundle>? = null

        fun message(@PropertyKey(resourceBundle = RESOURCE_BUNDLE) key: String, vararg params: Any): String =
                BundleBase.message(getBundle(), key, *params)

        fun derivedResourceManager(project: Project): DerivedResourceManager =
                ServiceManager.getService(project, DerivedResourceManager::class.java)

        fun classIdCache(project: Project): ClassIdCache = ServiceManager.getService(project, ClassIdCache::class.java)

        private fun getBundle(): ResourceBundle {
            val cachedBundle = com.intellij.reference.SoftReference.dereference(ourBundle)
            if (cachedBundle != null) {
                return cachedBundle
            }

            val bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE)
            ourBundle = SoftReference(bundle)
            return bundle
        }
    }
}
