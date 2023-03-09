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

import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.types.JSAnyType
import com.intellij.lang.javascript.psi.types.primitives.JSNullType
import com.intellij.lang.javascript.psi.types.primitives.JSPrimitiveType
import com.intellij.lang.javascript.psi.types.primitives.JSUndefinedType
import org.eclipse.scout.sdk.core.typescript.TypeScriptTypes
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType
import org.eclipse.scout.sdk.core.typescript.model.api.internal.DataTypeImplementor
import org.eclipse.scout.sdk.core.typescript.model.spi.AbstractNodeElementSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.DataTypeSpi
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.core.util.SourceRange
import java.util.*

open class IdeaJavaScriptType(protected val ideaModule: IdeaNodeModule, protected val javaScriptType: JSType) : AbstractNodeElementSpi<IDataType>(ideaModule), DataTypeSpi {

    private val m_name = FinalValue<String>()

    override fun createApi() = DataTypeImplementor(this)

    override fun source() = Optional.of(SourceRange(name(), 0))

    override fun name(): String = m_name.computeIfAbsentAndGet { if (javaScriptType is JSAnyType) TypeScriptTypes._any else javaScriptType.typeText }

    override fun isPrimitive() = (javaScriptType as? JSPrimitiveType)?.isPrimitive ?: (javaScriptType is JSUndefinedType || javaScriptType is JSNullType)
}