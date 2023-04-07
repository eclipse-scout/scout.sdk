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

import com.intellij.lang.javascript.psi.JSVariable
import org.eclipse.scout.sdk.core.typescript.model.api.IVariable
import org.eclipse.scout.sdk.core.typescript.model.api.Modifier
import org.eclipse.scout.sdk.core.typescript.model.api.internal.VariableImplementor
import org.eclipse.scout.sdk.core.typescript.model.spi.AbstractNodeElementSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.DataTypeSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.VariableSpi
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.core.util.SourceRange
import org.eclipse.scout.sdk.s2i.model.typescript.util.DataTypeSpiUtils
import org.eclipse.scout.sdk.s2i.model.typescript.util.exportType
import org.eclipse.scout.sdk.s2i.model.typescript.util.toModifierType
import org.eclipse.scout.sdk.s2i.resolveLocalPath
import java.util.*

open class IdeaJavaScriptVariable(protected val ideaModule: IdeaNodeModule, internal val javaScriptVariable: JSVariable) : AbstractNodeElementSpi<IVariable>(ideaModule), VariableSpi {

    private val m_dataType = FinalValue<DataTypeSpi?>()
    private val m_source = FinalValue<Optional<SourceRange>>()

    override fun createApi() = VariableImplementor(this)

    override fun source(): Optional<SourceRange> = m_source.computeIfAbsentAndGet { ideaModule.sourceFor(javaScriptVariable) }

    override fun exportType() = javaScriptVariable.exportType()

    override fun resolveContainingFile() = javaScriptVariable.containingFile?.virtualFile?.resolveLocalPath()

    override fun hasModifier(modifier: Modifier) = javaScriptVariable.hasModifier(modifier.toModifierType())

    override fun name() = javaScriptVariable.name

    override fun dataType() = m_dataType.computeIfAbsentAndGet {
        DataTypeSpiUtils.createDataType(javaScriptVariable, ideaModule, this::constantValue)
    }

    override fun constantValue() = ideaModule.nodeElementFactory().createConstantValue(javaScriptVariable.initializer)
}