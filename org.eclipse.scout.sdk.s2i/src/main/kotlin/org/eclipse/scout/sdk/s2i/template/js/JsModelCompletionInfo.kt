/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.template.js

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.openapi.module.Module
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsCoreConstants
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsModel
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsModels
import org.eclipse.scout.sdk.core.s.model.js.objects.IScoutJsObject
import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsProperty
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class
import org.eclipse.scout.sdk.core.typescript.model.spi.ES6ClassSpi
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.s2i.model.typescript.IdeaNodeModule

data class JsModelCompletionInfo(
    val propertyPsi: JSProperty, val propertyName: String, val objectLiteral: JSObjectLiteralExpression, val module: Module, val scoutJsModel: ScoutJsModel, val isLast: Boolean,
    val isPropertyNameCompletion: Boolean, val siblingPropertyNames: Set<String>, val searchPrefix: String, val isInArray: Boolean, val isInLiteral: Boolean
) {
    private val m_referencedClass = FinalValue<IES6Class?>()
    private val m_objectTypeScoutObject = FinalValue<IScoutJsObject?>()
    private val m_objectTypeModel = FinalValue<ScoutJsModel?>()
    private val m_properties = FinalValue<List<ScoutJsProperty>>()
    private val m_parentScoutProperty = FinalValue<ScoutJsProperty?>()

    fun availableProperties() = m_properties.computeIfAbsentAndGet {
        val scoutObject = declaringScoutObjectByObjectType()
        if (scoutObject != null) return@computeIfAbsentAndGet scoutObject.findProperties()
            .withSuperClasses(true)
            .stream().toList()

        val parentProperty = parentScoutProperty() ?: return@computeIfAbsentAndGet emptyList()
        return@computeIfAbsentAndGet parentProperty.type().possibleChildProperties().toList()
    }.stream()

    fun parentScoutProperty(): ScoutJsProperty? = m_parentScoutProperty.computeIfAbsentAndGet {
        val infoForParentObject = JsModelCompletionHelper.getPropertyValueInfo(propertyPsi, searchPrefix) ?: return@computeIfAbsentAndGet null
        infoForParentObject.availableProperties()
            .filter { it.name() == infoForParentObject.propertyName }
            .filter { it.type().isChildModelSupported }
            .findAny()
            .orElse(null)
    }

    private fun declaringScoutObjectByObjectType() = m_objectTypeScoutObject.computeIfAbsentAndGet {
        val referencedClass = findReferencedClass() ?: return@computeIfAbsentAndGet null
        val objectTypeModel = objectTypeModel() ?: return@computeIfAbsentAndGet null
        objectTypeModel
            .findScoutObjects()
            .withDeclaringClass(referencedClass)
            .first()
            .orElse(null)
    }

    private fun objectTypeModel() = m_objectTypeModel.computeIfAbsentAndGet { findReferencedClass()?.containingModule().let { ScoutJsModels.create(it).orElse(null) } }

    private fun findReferencedClass(): IES6Class? = m_referencedClass.computeIfAbsentAndGet {
        val ref = objectLiteral.findProperty(ScoutJsCoreConstants.PROPERTY_NAME_OBJECT_TYPE)?.value ?: return@computeIfAbsentAndGet null
        if (ref is JSReferenceExpression) {
            val ideaNodeModule = scoutJsModel.nodeModule().spi() as IdeaNodeModule
            val referencedClass = ideaNodeModule.resolveReferencedElement(ref) as? ES6ClassSpi ?: return@computeIfAbsentAndGet null
            return@computeIfAbsentAndGet referencedClass.api()
        }

        if (ref is JSLiteralExpression) {
            val objectType = ref.takeIf { it.isStringLiteral }?.stringValue ?: return@computeIfAbsentAndGet null
            return@computeIfAbsentAndGet scoutJsModel.findScoutObjects()
                .withObjectType(objectType)
                .withIncludeDependencies(true)
                .first()
                .map {
                    m_objectTypeScoutObject.set(it)
                    m_objectTypeModel.set(it.scoutJsModel())
                    it.declaringClass()
                }.orElse(null)
        }
        return@computeIfAbsentAndGet null
    }
}
