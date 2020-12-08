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
package org.eclipse.scout.sdk.s2i.util

import com.intellij.codeInsight.completion.JavaCompletionContributor
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CodeStyleSettingsManager
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.log.SdkLog.onTrace
import org.eclipse.scout.sdk.core.util.FinalValue
import java.lang.reflect.Method

/**
 * Holds all API version dependent logic.
 */
object CompatibilityHelper {

    /**
     * Temporary settings creation changed in 2020.2
     * Can be removed if the minimal supported IJ version is >= 2020.2
     */
    private val CREATE_TEMP_SETTINGS_METHOD = FinalValue<Method>()

    fun createTempSettings(origSettings: CodeStyleSettings, settingsManager: CodeStyleSettingsManager): CodeStyleSettings {
        val createTemporarySettings = CREATE_TEMP_SETTINGS_METHOD.computeIfAbsentAndGet { createTemporarySettingsMethod() }
        if (createTemporarySettings != null) {
            // use createTemporarySettings() factory method in IJ 2020.2 and newer. The created settings are already activated.
            val tempSettings = createTemporarySettings.invoke(settingsManager) as CodeStyleSettings
            tempSettings.copyFrom(origSettings)
            return tempSettings
        }

        // use clone method until IJ 2020.1
        val cloned = CodeStyleSettings::class.java.getMethod("clone").invoke(origSettings) as CodeStyleSettings
        settingsManager.setTemporarySettings(cloned)
        return cloned
    }

    private fun createTemporarySettingsMethod() =
            try {
                CodeStyleSettingsManager::class.java.getMethod("createTemporarySettings")
            } catch (e: NoSuchMethodException) {
                SdkLog.debug("Using legacy temporary CodeStyleSettings creation.", onTrace(e))
                null
            }

    /**
     * semicolonNeeded method changed in IJ 2020.2
     * Can be removed if the minimal supported IJ version is 2020.2
     */
    val SEMICOLON_NEEDED_METHOD = FinalValue<Method>()

    fun semicolonNeeded(editor: Editor, file: PsiFile, startOffset: Int): Boolean {
        val semicolonNeededNew = SEMICOLON_NEEDED_METHOD.computeIfAbsentAndGet { semicolonNeededNew() }
        return if (semicolonNeededNew != null) {
            semicolonNeededNew.invoke(null, file, startOffset) as Boolean
        } else {
            semicolonNeededLegacy().invoke(null, editor, file, startOffset) as Boolean
        }
    }

    private fun semicolonNeededNew() =
            try {
                JavaCompletionContributor::class.java.getMethod("semicolonNeeded", PsiFile::class.java, Int::class.java)
            } catch (e: NoSuchMethodException) {
                SdkLog.debug("Using legacy JavaCompletionContributor.semicolonNeeded() method.", onTrace(e))
                null
            }

    private fun semicolonNeededLegacy() = JavaCompletionContributor::class.java.getMethod("semicolonNeeded", Editor::class.java, PsiFile::class.java, Int::class.java)
}
