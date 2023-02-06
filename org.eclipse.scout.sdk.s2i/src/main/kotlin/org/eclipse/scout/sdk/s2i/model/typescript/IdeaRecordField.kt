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

import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSRecordType.PropertySignature
import com.intellij.psi.util.PsiTreeUtil
import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue
import org.eclipse.scout.sdk.core.typescript.model.api.Modifier
import org.eclipse.scout.sdk.core.typescript.model.spi.AbstractFieldSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.DataTypeSpi
import org.eclipse.scout.sdk.core.util.FinalValue

open class IdeaRecordField(protected val ideaModule: IdeaNodeModule, internal val property: PropertySignature) : AbstractFieldSpi(ideaModule) {

    private val m_dataType = FinalValue<DataTypeSpi?>()
    private val m_jsProperty = FinalValue<JSProperty?>()
    private val m_constantValue = FinalValue<IConstantValue>()

    override fun source() = ideaModule.sourceFor(getJsProperty())

    override fun name() = property.memberName

    override fun hasModifier(modifier: Modifier?) = false

    override fun isOptional() = property.isOptional

    override fun dataTypeImpl() = m_dataType.computeIfAbsentAndGet {
        ideaModule.dataTypeFactory.createDataType(property)
    }

    override fun constantValue(): IConstantValue = m_constantValue.computeIfAbsentAndGet {
        ideaModule.spiFactory.createConstantValue(getJsProperty()?.value, ideaModule)
    }

    protected fun getJsProperty() = m_jsProperty.computeIfAbsentAndGet {
        property.jsType
            ?.sourceElement
            ?.let { PsiTreeUtil.getParentOfType(it, JSProperty::class.java) }
    }
}