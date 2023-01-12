/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "ScoutCodeFoldingSettings", storages = [Storage("editor.xml")])
class ScoutCodeFoldingSettings : PersistentStateComponent<ScoutCodeFoldingSettings> {
    private var m_collapseTranslations = true

    companion object {
        fun getInstance(): ScoutCodeFoldingSettings = ApplicationManager.getApplication().getService(ScoutCodeFoldingSettings::class.java)
    }

    fun isCollapseTranslations(): Boolean {
        return m_collapseTranslations
    }

    fun setCollapseTranslations(value: Boolean) {
        m_collapseTranslations = value
    }

    override fun getState() = this

    override fun loadState(state: ScoutCodeFoldingSettings) = XmlSerializerUtil.copyBean(state, this)
}