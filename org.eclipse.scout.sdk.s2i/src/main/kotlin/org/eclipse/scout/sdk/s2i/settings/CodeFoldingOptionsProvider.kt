/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.settings

import com.intellij.application.options.editor.CodeFoldingOptionsProvider
import com.intellij.openapi.options.BeanConfigurable
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message

class ScoutCodeFoldingOptionsProvider : BeanConfigurable<ScoutCodeFoldingSettings>(ScoutSettingsHelper.getCodeFoldingSettings(), "Scout"), CodeFoldingOptionsProvider {
    init {
        checkBox(message("nls.folding.name"), instance::isCollapseTranslations, instance::setCollapseTranslations)
    }
}