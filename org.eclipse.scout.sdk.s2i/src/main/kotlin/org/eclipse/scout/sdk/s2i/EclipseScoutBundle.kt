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
package org.eclipse.scout.sdk.s2i

import com.intellij.AbstractBundle
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.ui.IconManager
import com.intellij.util.IconUtil
import org.eclipse.scout.sdk.s2i.classid.AutoCreateClassIdListener
import org.eclipse.scout.sdk.s2i.classid.ClassIdCache
import org.eclipse.scout.sdk.s2i.dataobject.DataObjectManager
import org.eclipse.scout.sdk.s2i.derived.DerivedResourceManager
import org.eclipse.scout.sdk.s2i.element.ElementCreationManager
import org.eclipse.scout.sdk.s2i.model.js.JsModuleCacheImplementor
import org.eclipse.scout.sdk.s2i.nls.TranslationStoreStackCache
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
private const val RESOURCE_BUNDLE = "messages.EclipseScoutBundle"

object EclipseScoutBundle : AbstractBundle(RESOURCE_BUNDLE) {

    val ScoutIcon = load("/META-INF/pluginIcon.svg")

    @Nls
    fun message(@PropertyKey(resourceBundle = RESOURCE_BUNDLE) key: String, vararg params: Any): String =
        getMessage(key, *params)

    fun derivedResourceManager(project: Project): DerivedResourceManager =
        project.getService(DerivedResourceManager::class.java)

    fun dataObjectManager(project: Project): DataObjectManager =
        project.getService(DataObjectManager::class.java)

    fun autoCreateClassIdListener(project: Project): AutoCreateClassIdListener =
        project.getService(AutoCreateClassIdListener::class.java)

    fun classIdCache(project: Project): ClassIdCache =
        project.getService(ClassIdCache::class.java)

    fun translationStoreStackCache(project: Project): TranslationStoreStackCache =
        project.getService(TranslationStoreStackCache::class.java)

    fun jsModuleCache(project: Project): JsModuleCacheImplementor =
        project.getService(JsModuleCacheImplementor::class.java)

    fun elementCreationManager(): ElementCreationManager =
            ApplicationManager.getApplication().getService(ElementCreationManager::class.java)

    fun scoutIcon(size: Int) = IconUtil.scale(ScoutIcon, null, size / ScoutIcon.iconWidth.toFloat())

    private fun load(path: String) = IconManager.getInstance().getIcon(path, EclipseScoutBundle::class.java)
}
