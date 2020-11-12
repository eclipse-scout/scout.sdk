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

import com.intellij.application.options.editor.CodeFoldingOptionsProvider
import com.intellij.openapi.options.BeanConfigurable
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message

class ScoutCodeFoldingOptionsProvider : BeanConfigurable<ScoutCodeFoldingSettings>(ScoutSettings.getCodeFoldingSettings(), "Scout"), CodeFoldingOptionsProvider {
    init {
        val settings = instance
        if (settings != null) {
            checkBox(message("nls.folding.name"), settings::isCollapseTranslations, settings::setCollapseTranslations)
        }
    }
}