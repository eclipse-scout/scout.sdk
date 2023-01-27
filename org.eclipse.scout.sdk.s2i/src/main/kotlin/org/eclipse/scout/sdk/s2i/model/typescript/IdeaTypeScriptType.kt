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

import com.intellij.lang.javascript.psi.JSRecordType
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptObjectType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptPropertySignature
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeAlias
import com.intellij.lang.javascript.psi.types.JSRecursiveTypeVisitor
import com.intellij.lang.javascript.psi.types.TypeScriptTypeOfJSTypeImpl
import org.eclipse.scout.sdk.core.typescript.model.spi.FieldSpi

open class IdeaTypeScriptType(ideaModule: IdeaNodeModule, val typeScriptType: TypeScriptTypeAlias) : IdeaJavaScriptClass(ideaModule, typeScriptType) {

    override fun collectFields(collector: MutableCollection<FieldSpi>) {
        val typeDeclaration = typeScriptType.typeDeclaration
        if (typeDeclaration is TypeScriptObjectType) {
            // type with own fields
            typeDeclaration.children.asSequence()
                .mapNotNull { it as? TypeScriptPropertySignature }
                .map { ideaModule.spiFactory.createJavaScriptField(it) }
                .forEach { collector.add(it) }
        } else {
            // type alias with referenced fields (enum like)
            val parsedType = typeScriptType.parsedTypeDeclaration ?: return
            val recordType = firstChildOfType(parsedType, TypeScriptTypeOfJSTypeImpl::class.java)?.substitute() as? JSRecordType ?: return
            recordType.properties
                .forEach { collector.add(ideaModule.spiFactory.createRecordField(it)) }
        }
    }

    fun <T : JSType> firstChildOfType(startType: JSType, t: Class<T>): T? {
        var result: T? = null
        val visitor: JSRecursiveTypeVisitor = object : JSRecursiveTypeVisitor(false) {
            override fun visitJSType(type: JSType) {
                if (result == null && t.isAssignableFrom(type::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    result = type as T
                }
                if (result == null) {
                    super.visitJSType(type)
                }
            }
        }
        visitor.visitJSType(startType)
        return result
    }
}