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
package org.eclipse.scout.sdk.s2i

import com.intellij.ide.macro.Macro
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.PathManager

open class IdeaHomePathMacro : Macro() {

    override fun getName(): String = "IdeaHomePath"

    override fun getDescription(): String = "Installation Path of the running IntelliJ IDEA instance."

    override fun expand(dataContext: DataContext): String? = PathManager.getHomePath()
}