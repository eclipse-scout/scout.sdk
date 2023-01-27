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

import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.types.JSUtilType
import com.intellij.lang.javascript.psi.types.primitives.JSNullType
import com.intellij.lang.javascript.psi.types.primitives.JSPrimitiveType
import com.intellij.lang.javascript.psi.types.primitives.JSUndefinedType
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType
import org.eclipse.scout.sdk.core.typescript.model.api.internal.DataTypeImplementor
import org.eclipse.scout.sdk.core.typescript.model.spi.DataTypeSpi
import org.eclipse.scout.sdk.core.util.FinalValue

open class IdeaJavaScriptType(protected val javaScriptType: JSType) : DataTypeSpi {

    private val m_api = FinalValue<IDataType>()

    override fun api(): IDataType = m_api.computeIfAbsentAndGet { createApi() }

    protected fun createApi() = DataTypeImplementor(this)

    override fun name() = javaScriptType.typeText

    override fun isPrimitive() = (javaScriptType as? JSPrimitiveType)?.isPrimitive ?: (javaScriptType is JSUndefinedType || javaScriptType is JSNullType)

    companion object {

        fun parse(ideaModule: IdeaNodeModule, type: JSType): DataTypeSpi {
            (type as? JSUtilType)?.let { return ideaModule.spiFactory.createJavaScriptType(it) }

            (type.sourceElement as? JSElement)
                ?.let { (ideaModule.moduleInventory.resolveReferencedElement(it) as? DataTypeSpi) }
                ?.let { return it }

            return ideaModule.spiFactory.createJavaScriptType(type)
        }
    }
}