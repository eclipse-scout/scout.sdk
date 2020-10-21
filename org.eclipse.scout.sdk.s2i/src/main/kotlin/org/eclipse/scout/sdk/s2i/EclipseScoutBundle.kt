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

import com.intellij.AbstractBundle
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import org.eclipse.scout.sdk.s2i.classid.AutoCreateClassIdListener
import org.eclipse.scout.sdk.s2i.classid.ClassIdCache
import org.eclipse.scout.sdk.s2i.derived.DerivedResourceManager
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
private const val RESOURCE_BUNDLE = "messages.EclipseScoutBundle"

object EclipseScoutBundle : AbstractBundle(RESOURCE_BUNDLE) {

    @Nls
    fun message(@PropertyKey(resourceBundle = RESOURCE_BUNDLE) key: String, vararg params: Any): String =
            getMessage(key, *params)

    fun derivedResourceManager(project: Project): DerivedResourceManager =
            ServiceManager.getService(project, DerivedResourceManager::class.java)

    fun autoCreateClassIdListener(project: Project): AutoCreateClassIdListener =
            ServiceManager.getService(project, AutoCreateClassIdListener::class.java)

    fun classIdCache(project: Project): ClassIdCache = ServiceManager.getService(project, ClassIdCache::class.java)
}
