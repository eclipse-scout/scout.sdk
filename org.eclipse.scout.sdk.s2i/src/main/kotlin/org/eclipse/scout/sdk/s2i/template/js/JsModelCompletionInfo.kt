/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.template.js

import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.openapi.module.Module
import com.intellij.psi.util.PsiTreeUtil
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsCoreConstants
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsModel
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsModels
import org.eclipse.scout.sdk.core.s.model.js.objects.IScoutJsObject
import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsCoreDataTypesUnwrapVisitor
import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsProperty
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class
import org.eclipse.scout.sdk.core.typescript.model.spi.ES6ClassSpi
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.core.util.visitor.TreeVisitResult
import org.eclipse.scout.sdk.s2i.model.typescript.IdeaNodeModule

data class JsModelCompletionInfo(
    val propertyPsi: JSProperty, val propertyName: String, val objectLiteral: JSObjectLiteralExpression, val module: Module, val scoutJsModel: ScoutJsModel, val isLast: Boolean,
    val isPropertyNameCompletion: Boolean, val siblingPropertyNames: Set<String>, val searchPrefix: String, val extendedSearchPrefix: String?, val isInArray: Boolean, val isInLiteral: Boolean
) {
    private val m_referencedClass = FinalValue<IES6Class?>()
    private val m_objectTypeScoutObject = FinalValue<IScoutJsObject?>()
    private val m_objectTypeModel = FinalValue<ScoutJsModel?>()
    private val m_properties = FinalValue<List<ScoutJsProperty>>()
    private val m_parentScoutProperty = FinalValue<ScoutJsProperty?>()

    fun availableProperties() = m_properties.computeIfAbsentAndGet {
        val scoutObject = declaringScoutObjectByObjectType()
        if (scoutObject != null) {
            return@computeIfAbsentAndGet scoutObject.findProperties()
                .withSupers(true)
                .stream()
                .toList()
        }

        val parentProperty = parentScoutProperty() ?: return@computeIfAbsentAndGet emptyList()
        return@computeIfAbsentAndGet parentProperty.type().possibleChildProperties().toList()
    }.stream()

    fun parentScoutProperty(): ScoutJsProperty? = m_parentScoutProperty.computeIfAbsentAndGet {
        val parentProperty = PsiTreeUtil.getParentOfType(propertyPsi, JSProperty::class.java) ?: return@computeIfAbsentAndGet null
        val infoForParentObject = JsModelCompletionHelper.getPropertyValueInfo(parentProperty, searchPrefix) ?: return@computeIfAbsentAndGet null
        infoForParentObject.availableProperties()
            .filter { it.name() == infoForParentObject.propertyName }
            .filter { it.type().isChildModelSupported }
            .findAny()
            .orElse(null)
    }

    fun declaringScoutObjectByObjectType() = m_objectTypeScoutObject.computeIfAbsentAndGet {
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
        val objectType = objectLiteral.findProperty(ScoutJsCoreConstants.PROPERTY_NAME_OBJECT_TYPE)?.value?.let { resolveObjectTypeClass(it) }
        if (objectType != null) {
            // object type is declared directly in the current objectLiteral
            return@computeIfAbsentAndGet objectType
        }

        // the current objectLiteral has no own objectType. Try to resolve the type based on the parent property data type
        val propertyTypeAsDeclaredInParent = parentScoutProperty()?.type()?.dataType()?.orElse(null) ?: return@computeIfAbsentAndGet null
        val collector = HashSet<IES6Class>()
        propertyTypeAsDeclaredInParent.visit(ScoutJsCoreDataTypesUnwrapVisitor { childType, _, _ ->
            if (childType is IES6Class) {
                collector.add(childType)
            }
            return@ScoutJsCoreDataTypesUnwrapVisitor TreeVisitResult.CONTINUE
        })

        // the data type of the parent property might declare multiple classes
        // for now: if it is unique, use it as data type of the current object literal
        return@computeIfAbsentAndGet if (collector.size == 1) collector.first() else null
    }

    private fun resolveObjectTypeClass(objectTypeExpression: JSExpression): IES6Class? {
        if (objectTypeExpression is JSLiteralExpression) {
            val objectType = objectTypeExpression.takeIf { it.isStringLiteral }?.stringValue ?: return null
            // object type as string
            return scoutJsModel.findScoutObjects()
                .withObjectType(objectType)
                .withIncludeDependencies(true)
                .first()
                .map {
                    m_objectTypeScoutObject.set(it)
                    m_objectTypeModel.set(it.scoutJsModel())
                    it.declaringClass()
                }.orElse(null)
        }

        // object type as class reference (may contain generics!)
        val ideaNodeModule = scoutJsModel.nodeModule().spi() as IdeaNodeModule
        val referencedClass = ideaNodeModule.resolveReferencedElement(objectTypeExpression) as? ES6ClassSpi
        return referencedClass?.api()
    }
}
