/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i.settings

import com.intellij.application.options.editor.CodeFoldingConfigurable
import com.intellij.ide.ui.search.SearchableOptionContributor
import com.intellij.ide.ui.search.SearchableOptionProcessor
import com.intellij.openapi.application.ApplicationBundle

class ScoutSearchableOptionContributor : SearchableOptionContributor() {
    override fun processOptions(processor: SearchableOptionProcessor) {
        // Add option for Scout code folding settings to be found when searching
        processor.addOptions("scout texts", null, "scout texts", CodeFoldingConfigurable.ID, ApplicationBundle.message("group.code.folding"), false)
    }
}