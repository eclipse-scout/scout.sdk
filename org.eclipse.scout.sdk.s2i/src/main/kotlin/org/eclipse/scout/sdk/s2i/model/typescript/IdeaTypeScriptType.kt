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

import com.intellij.lang.javascript.psi.ecma6.TypeScriptObjectType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptPropertySignature
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeAlias
import org.eclipse.scout.sdk.core.typescript.model.spi.FieldSpi

open class IdeaTypeScriptType(ideaModule: IdeaNodeModule, val typeScriptType: TypeScriptTypeAlias) : IdeaJavaScriptClass(ideaModule, typeScriptType) {

    override fun collectFields(collector: MutableCollection<FieldSpi>) {
        val objectType = typeScriptType.typeDeclaration as? TypeScriptObjectType ?: return
        objectType.children.asSequence()
            .mapNotNull { it as? TypeScriptPropertySignature }
            .map { ideaModule.spiFactory.createJavaScriptField(it) }
            .forEach { collector.add(it) }
    }
}