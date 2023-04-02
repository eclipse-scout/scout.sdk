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

import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeofType
import org.eclipse.scout.sdk.core.typescript.model.api.ITypeOf
import org.eclipse.scout.sdk.core.typescript.model.api.internal.TypeOfImplementor
import org.eclipse.scout.sdk.core.typescript.model.spi.AbstractNodeElementSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.DataTypeOwnerSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.DataTypeSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.TypeOfSpi
import org.eclipse.scout.sdk.core.util.FinalValue

open class IdeaTypeScriptTypeofType(protected val ideaModule: IdeaNodeModule, internal val typeOfType: TypeScriptTypeofType) : AbstractNodeElementSpi<ITypeOf>(ideaModule), TypeOfSpi {

    private val m_dataTypeOwner = FinalValue<DataTypeOwnerSpi?>()

    override fun createApi() = TypeOfImplementor(this)

    override fun name() = "typeof " + typeOfType.referenceText

    override fun childTypes(): Collection<DataTypeSpi> = listOfNotNull(dataType())

    override fun source() = ideaModule.sourceFor(typeOfType)

    override fun dataTypeOwner() = m_dataTypeOwner.computeIfAbsentAndGet {
        val expression = typeOfType.expression ?: return@computeIfAbsentAndGet null
        ideaModule.resolveReferencedElement(expression) as? DataTypeOwnerSpi
    }
}