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

import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameter
import org.eclipse.scout.sdk.core.typescript.model.api.INodeElement.ExportType
import org.eclipse.scout.sdk.core.typescript.model.api.ITypeParameter
import org.eclipse.scout.sdk.core.typescript.model.api.internal.TypeParameterImplementor
import org.eclipse.scout.sdk.core.typescript.model.spi.AbstractNodeElementSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.DataTypeSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.ES6ClassSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.TypeParameterSpi
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.core.util.SourceRange
import org.eclipse.scout.sdk.s2i.model.typescript.util.DataTypeSpiUtils
import java.nio.file.Path
import java.util.*

open class IdeaTypeParameter(protected val ideaModule: IdeaNodeModule, internal val typeParameter: TypeScriptTypeParameter, internal val ownerClass: ES6ClassSpi) :
    AbstractNodeElementSpi<ITypeParameter>(ideaModule), TypeParameterSpi {

    private val m_constraint = FinalValue<DataTypeSpi?>()
    private val m_default = FinalValue<DataTypeSpi?>()
    private val m_source = FinalValue<Optional<SourceRange>>()

    override fun resolveContainingFile(): Path? = ownerClass.containingFile().orElse(null)

    override fun createApi() = TypeParameterImplementor(this)

    override fun exportType() = ExportType.NONE

    override fun source(): Optional<SourceRange> = m_source.computeIfAbsentAndGet { ideaModule.sourceFor(typeParameter) }

    override fun declaringClass() = ownerClass

    override fun name() = typeParameter.name

    override fun constraint() = m_constraint.computeIfAbsentAndGet {
        typeParameter.typeConstraint?.let { DataTypeSpiUtils.createDataType(it.jsType, ideaModule) }
    }

    override fun defaultConstraint() = m_default.computeIfAbsentAndGet {
        typeParameter.default?.let { DataTypeSpiUtils.createDataType(it.jsType, ideaModule) }
    }
}