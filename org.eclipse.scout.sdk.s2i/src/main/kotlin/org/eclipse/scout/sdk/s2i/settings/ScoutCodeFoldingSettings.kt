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
package org.eclipse.scout.sdk.s2i.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "ScoutCodeFoldingSettings", storages = [Storage("editor.xml")])
class ScoutCodeFoldingSettings : PersistentStateComponent<ScoutCodeFoldingSettings> {
    private var m_collapseTranslations = true

    companion object {
        fun getInstance(): ScoutCodeFoldingSettings = ServiceManager.getService(ScoutCodeFoldingSettings::class.java)
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