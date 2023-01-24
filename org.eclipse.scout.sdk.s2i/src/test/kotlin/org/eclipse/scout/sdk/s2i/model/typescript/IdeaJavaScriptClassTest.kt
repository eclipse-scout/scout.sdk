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

import com.intellij.lang.javascript.psi.ecmal4.JSClass
import org.eclipse.scout.sdk.core.typescript.model.spi.ES6ClassSpi
import org.eclipse.scout.sdk.s2i.model.AbstractES6ClassTest

class IdeaJavaScriptClassTest : AbstractES6ClassTest("model/javascript/es6class/SomeClass.js") {

    override fun createES6ClassSpi(): ES6ClassSpi = IdeaJavaScriptClass(ideaModule, findChildOfType(JSClass::class.java))

    override fun isAssertOptional(): Boolean = false
}