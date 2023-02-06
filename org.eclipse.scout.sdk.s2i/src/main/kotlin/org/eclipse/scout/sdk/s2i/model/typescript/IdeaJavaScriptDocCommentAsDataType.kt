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

import org.eclipse.scout.sdk.core.typescript.TypeScriptTypes
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType
import org.eclipse.scout.sdk.core.typescript.model.api.internal.DataTypeImplementor
import org.eclipse.scout.sdk.core.typescript.model.spi.DataTypeSpi
import org.eclipse.scout.sdk.core.util.FinalValue

open class IdeaJavaScriptDocCommentAsDataType internal constructor(val dataType: String) : DataTypeSpi {

    private val m_api = FinalValue<IDataType>()

    override fun api(): IDataType = m_api.computeIfAbsentAndGet { createApi() }

    protected fun createApi() = DataTypeImplementor(this)

    override fun name() = dataType

    override fun isPrimitive() = TypeScriptTypes.isPrimitive(dataType)
}