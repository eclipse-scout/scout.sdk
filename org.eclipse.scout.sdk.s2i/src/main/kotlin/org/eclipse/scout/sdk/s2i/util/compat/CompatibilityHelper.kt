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
package org.eclipse.scout.sdk.s2i.util.compat

import com.intellij.codeInsight.completion.JavaCompletionContributor
import com.intellij.find.impl.RegExHelpPopup
import com.intellij.notification.NotificationGroup
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CodeStyleSettingsManager
import com.intellij.ui.components.labels.LinkLabel
import java.awt.Component

/**
 * Holds all API version dependent logic.
 */
object CompatibilityHelper {

    /**
     * createRegExLink() method changed with IJ 2021.1
     * Can be removed if the minimal supported IJ version is >= 2021.1
     */
    private val REGEX_LINK_HELP_CREATOR = CompatibilityMethodCaller<LinkLabel<*>>()
            .withCandidate(RegExHelpPopup::class.java, "createRegExLink", String::class.java, Component::class.java) {
                it.invokeStatic(it.args[0], it.args[1]) // >= IJ 2021.1
            }
            .withCandidate(RegExHelpPopup::class.java, "createRegExLink", String::class.java, Component::class.java, Logger::class.java) {
                it.invokeStatic(it.args[0], it.args[1], null) // < IJ 2021.1
            }

    fun createRegExLink(title: String, owner: Component) = REGEX_LINK_HELP_CREATOR.invoke(title, owner)

    /**
     * NotificationGroup creation changed with IJ 2020.3
     * Can be removed if the minimal supported IJ version is >= 2020.3
     */
    private val SCOUT_BALLOON_GROUP = CompatibilityMethodCaller<NotificationGroup>()
            .withCandidate("com.intellij.notification.NotificationGroupManager", "getNotificationGroup", String::class.java.name) {
                val notificationGroupManager = it.descriptor.resolvedClass().getMethod("getInstance").invoke(null)
                it.invoke(notificationGroupManager, "scout.notification")
            }
            .withCandidate(NotificationGroup::class.java, "balloonGroup", String::class.java) {
                it.invokeStatic("Scout")
            }

    fun balloonGroup() = SCOUT_BALLOON_GROUP.invoke()

    /**
     * Temporary settings creation changed in 2020.2
     * Can be removed if the minimal supported IJ version is >= 2020.2
     */
    private val CREATE_TEMP_SETTINGS_METHOD = CompatibilityMethodCaller<CodeStyleSettings>()
            .withCandidate(CodeStyleSettingsManager::class.java, "createTemporarySettings") {
                // use createTemporarySettings() factory method in IJ 2020.2 and newer. The created settings are already activated.
                val origSettings = it.args[0] as CodeStyleSettings
                val settingsManager = it.args[1]
                val tempSettings = it.invoke(settingsManager)
                tempSettings.copyFrom(origSettings)
                return@withCandidate tempSettings
            }
            .withCandidate(CodeStyleSettings::class.java, "clone") {
                // use clone method until IJ 2020.1
                val origSettings = it.args[0]
                val settingsManager = it.args[1] as CodeStyleSettingsManager
                val cloned = it.invoke(origSettings)
                settingsManager.setTemporarySettings(cloned)
                return@withCandidate cloned
            }

    fun createTempSettings(origSettings: CodeStyleSettings, settingsManager: CodeStyleSettingsManager) = CREATE_TEMP_SETTINGS_METHOD.invoke(origSettings, settingsManager)

    /**
     * semicolonNeeded method changed in IJ 2020.2
     * Can be removed if the minimal supported IJ version is 2020.2
     */
    private val SEMICOLON_NEEDED_METHOD = CompatibilityMethodCaller<Boolean>()
            .withCandidate(JavaCompletionContributor::class.java, "semicolonNeeded", PsiFile::class.java, Int::class.java) {
                val (_, file, startOffset) = it.args
                it.invokeStatic(file, startOffset)
            }
            .withCandidate(JavaCompletionContributor::class.java, "semicolonNeeded", Editor::class.java, PsiFile::class.java, Int::class.java) {
                val (editor, file, startOffset) = it.args
                it.invokeStatic(editor, file, startOffset)
            }

    fun semicolonNeeded(editor: Editor, file: PsiFile, startOffset: Int) = SEMICOLON_NEEDED_METHOD.invoke(editor, file, startOffset)
}
