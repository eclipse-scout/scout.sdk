/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.model.typescript

import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import org.eclipse.scout.sdk.s2i.model.AbstractES6ClassTest

class IdeaTypeScriptClassTest : AbstractES6ClassTest("model/typescript/es6class/SomeClass.ts") {

    override fun createES6ClassSpi() = IdeaTypeScriptClass(ideaModule, findChildOfType(TypeScriptClass::class.java))
}