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

import com.intellij.ide.macro.Macro
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.PathManager
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message

open class IdeaHomePathMacro : Macro() {

    override fun getName(): String = "IdeaHomePath"

    override fun getDescription(): String = message("idea.home.path.macro.desc")

    override fun expand(dataContext: DataContext): String? = PathManager.getHomePath()
}