/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.settings

import com.intellij.ide.ui.search.SearchableOptionContributor
import com.intellij.ide.ui.search.SearchableOptionProcessor
import com.intellij.openapi.application.ApplicationBundle

class ScoutSearchableOptionContributor : SearchableOptionContributor() {
    override fun processOptions(processor: SearchableOptionProcessor) {
        // Add option for Scout code folding settings to be found when searching
        val id = "editor.preferences.folding" // from CodeFoldingConfigurable.ID which is internal in some IJ versions
        processor.addOptions("scout texts", null, "scout texts", id, ApplicationBundle.message("group.code.folding"), false)
    }
}