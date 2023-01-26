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
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.lang.javascript.psi.util.JSClassUtils
import org.eclipse.scout.sdk.core.typescript.model.spi.FieldSpi

open class IdeaTypeScriptClass(ideaModule: IdeaNodeModule, typeScriptClass: TypeScriptClass) : IdeaJavaScriptClass(ideaModule, typeScriptClass) {

    override fun collectFields(collector: MutableCollection<FieldSpi>) =
        javaScriptClass.fields.asSequence()
            .mapNotNull { it as? TypeScriptField }
            .filter { !JSClassUtils.isStaticMethodOrField(it) }
            .map { ideaModule.spiFactory.createJavaScriptField(it) }
            .forEach { collector.add(it) }
}