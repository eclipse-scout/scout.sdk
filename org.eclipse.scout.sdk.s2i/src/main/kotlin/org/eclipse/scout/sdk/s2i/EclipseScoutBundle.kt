/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i

import com.intellij.AbstractBundle
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.project.Project
import com.intellij.ui.IconManager
import com.intellij.util.IconUtil
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.classid.AutoCreateClassIdListener
import org.eclipse.scout.sdk.s2i.classid.ClassIdCache
import org.eclipse.scout.sdk.s2i.dataobject.DataObjectManager
import org.eclipse.scout.sdk.s2i.derived.DerivedResourceManager
import org.eclipse.scout.sdk.s2i.element.ElementCreationManager
import org.eclipse.scout.sdk.s2i.nls.TranslationManagerCache
import org.eclipse.scout.sdk.s2i.util.compat.CompatibilityMethodCaller
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import javax.swing.Icon

@NonNls
private const val RESOURCE_BUNDLE = "messages.EclipseScoutBundle"

object EclipseScoutBundle : AbstractBundle(RESOURCE_BUNDLE) {

    val ScoutIcon = load("META-INF/pluginIcon.svg")

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

    fun translationStoreManagerCache(project: Project): TranslationManagerCache =
        project.getService(TranslationManagerCache::class.java)

    fun elementCreationManager(): ElementCreationManager =
        ApplicationManager.getApplication().getService(ElementCreationManager::class.java)

    fun scoutIcon(size: Int) = IconUtil.scale(ScoutIcon, null, size / ScoutIcon.iconWidth.toFloat())

    fun isRunningInSandbox(): Boolean {
        val sandbox = "plugins-sandbox"
        return Strings.countMatches(PathManager.getPluginsPath(), sandbox) > 0
                || Strings.countMatches(PathManager.getConfigPath(), sandbox) > 0
                || Strings.countMatches(PathManager.getSystemPath(), sandbox) > 0
    }

    /**
     * can be removed as soon as IJ 2023.2 is the latest supported version
     */
    private fun load(path: String) = CompatibilityMethodCaller<Icon>()
        .withCandidate(IconManager::class.java, "getIcon", String::class.java, ClassLoader::class.java) {
            // for IJ >= 2023.2 use ClassLoader as second argument
            it.invoke(IconManager.getInstance(), path, javaClass.classLoader)
        }.withCandidate(IconManager::class.java, "getIcon", String::class.java, Class::class.java) {
            // for IJ <= 2023.1: use Class as second argument
            it.invoke(IconManager.getInstance(), path, javaClass)
        }.invoke()
}
