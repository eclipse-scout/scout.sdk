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

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.TypeScriptAsExpression
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluator
import com.intellij.lang.javascript.psi.types.JSLiteralType
import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class
import org.eclipse.scout.sdk.core.typescript.model.api.IObjectLiteral
import org.eclipse.scout.sdk.core.typescript.model.spi.ES6ClassSpi
import org.eclipse.scout.sdk.core.util.FinalValue
import java.math.BigInteger
import java.util.*

open class IdeaConstantValue(protected val ideaModule: IdeaNodeModule, internal val element: JSElement?) : IConstantValue {

    private val m_unwrappedElement = FinalValue<JSElement?>()
    private val m_referencedES6Class = FinalValue<ES6ClassSpi?>()
    private val m_referencedConstantValue = FinalValue<IdeaConstantValue?>()

    private val m_type = FinalValue<IConstantValue.ConstantValueType>()
    private val m_dataType = FinalValue<IDataType?>()

    fun unwrappedElement() = m_unwrappedElement.computeIfAbsentAndGet {
        val unwrappedElement = unwrapTypeScriptAsExpression(element)
        unwrapJSNewExpression(unwrappedElement)
    }

    protected fun unwrapTypeScriptAsExpression(element: JSElement?) = if (element is TypeScriptAsExpression) element.expression else element

    protected fun unwrapJSNewExpression(element: JSElement?) = if (element is JSNewExpression) element.methodExpression else element

    fun referencedES6Class() = m_referencedES6Class.computeIfAbsentAndGet {
        (unwrappedElement() as? JSReferenceExpression)?.let { ideaModule.moduleInventory.resolveReferencedElement(it) as? ES6ClassSpi }
    }

    fun referencedConstantValue() = m_referencedConstantValue.computeIfAbsentAndGet {
        referencedES6Class()?.let { return@computeIfAbsentAndGet null }
        (unwrappedElement() as? JSReferenceExpression)
            ?.let {
                val reference = ideaModule.resolveReferenceTarget(it)
                if (reference === it) return@computeIfAbsentAndGet null
                reference
            }
            ?.let { if (it is JSProperty) it.value else it }
            ?.let { ideaModule.moduleInventory.findContainingModule(it)?.let { containingModule -> it to containingModule } }
            ?.let { ideaModule.spiFactory.createConstantValue(it.first, it.second) }
    }

    override fun <T : Any> convertTo(expectedType: Class<T>?): Optional<T> {
        if (expectedType == null) return Optional.empty()

        referencedConstantValue()?.convertTo(expectedType)?.let { return it }

        val converted: Any?
        if (expectedType.isArray) {
            converted = tryConvertToArray(expectedType)
        } else {
            converted = when (expectedType) {
                IObjectLiteral::class.java -> tryConvertToObjectLiteral()

                IES6Class::class.java -> tryConvertToES6Class()

                String::class.java,
                java.lang.String::class.java -> tryConvertToString()

                Boolean::class.java,
                java.lang.Boolean::class.java -> tryConvertToBoolean()

                java.lang.Integer::class.java,
                Int::class.java,
                java.lang.Float::class.java,
                Float::class.java,
                java.lang.Long::class.java,
                Long::class.java,
                java.lang.Double::class.java,
                Double::class.java,
                java.math.BigDecimal::class.java,
                java.math.BigInteger::class.java -> tryConvertToNumber(expectedType)

                else -> null // unknown type conversion
            }
        }

        @Suppress("UNCHECKED_CAST") // do not use expectedType.cast() here
        return Optional.ofNullable(converted as T?)
    }

    override fun type(): IConstantValue.ConstantValueType = m_type.computeIfAbsentAndGet {
        val unwrappedElement = unwrappedElement()
        if (unwrappedElement is JSObjectLiteralExpression) return@computeIfAbsentAndGet IConstantValue.ConstantValueType.ObjectLiteral
        if (unwrappedElement is JSArrayLiteralExpression) return@computeIfAbsentAndGet IConstantValue.ConstantValueType.Array
        referencedES6Class()?.let { return@computeIfAbsentAndGet IConstantValue.ConstantValueType.ES6Class }
        referencedConstantValue()?.let { return@computeIfAbsentAndGet it.type() }

        // literal values
        if (unwrappedElement !is JSLiteralExpression) return@computeIfAbsentAndGet IConstantValue.ConstantValueType.Unknown
        if (unwrappedElement.isStringLiteral) return@computeIfAbsentAndGet IConstantValue.ConstantValueType.String
        if (unwrappedElement.isBooleanLiteral) return@computeIfAbsentAndGet IConstantValue.ConstantValueType.Boolean
        if (unwrappedElement.isNumericLiteral) return@computeIfAbsentAndGet IConstantValue.ConstantValueType.Numeric
        return@computeIfAbsentAndGet IConstantValue.ConstantValueType.Unknown
    }

    override fun dataType(): Optional<IDataType> = Optional.ofNullable(m_dataType.computeIfAbsentAndGet {
        referencedConstantValue()
            ?.let { it.element?.parent as? JSProperty }
            ?.let { it.parent as? JSObjectLiteralExpression }
            ?.let {
                val name = when (val parent = it.parent) {
                    is JSFieldVariable -> parent.name
                    is JSProperty -> parent.name
                    else -> null
                } ?: return@let null
                return@computeIfAbsentAndGet ideaModule.spiFactory.createObjectLiteralDataType(name, it).api()
            }

        referencedConstantValue()?.let { return@computeIfAbsentAndGet it.dataType().orElse(null) }

        when (type()) {
            IConstantValue.ConstantValueType.Boolean,
            IConstantValue.ConstantValueType.Numeric,
            IConstantValue.ConstantValueType.String -> (unwrappedElement() as? JSExpression)
                ?.let { JSTypeEvaluator.getTypeFromConstant(it) }
                ?.let { if (it is JSLiteralType) it.asPrimitiveType() else it }
                ?.let { ideaModule.spiFactory.createJavaScriptType(it) }?.api()

            IConstantValue.ConstantValueType.ES6Class -> tryConvertToES6Class()

            IConstantValue.ConstantValueType.ObjectLiteral,
            IConstantValue.ConstantValueType.Array,
            IConstantValue.ConstantValueType.Unknown -> null
        }
    })

    protected fun tryConvertToES6Class(): IES6Class? = referencedES6Class()?.api()

    @Suppress("UNCHECKED_CAST")
    protected fun <T> tryConvertToArray(expectedType: Class<T>): Array<Any?>? {
        val arrayLiteral = unwrappedElement() as? JSArrayLiteralExpression ?: return null
        val expressions = arrayLiteral.expressions
        val componentType = expectedType.componentType
        val result = java.lang.reflect.Array.newInstance(componentType, expressions.size) as Array<Any?>
        for (i in expressions.indices) {
            val value = ideaModule.spiFactory.createConstantValue(expressions[i], ideaModule)
            if (IConstantValue::class.java == componentType) {
                result[i] = value
            } else {
                result[i] = value.convertTo(componentType).orElse(null)
            }
        }
        return result
    }

    protected fun tryConvertToString(): String? {
        val literal = unwrappedElement() as? JSLiteralExpression ?: return null
        return literal.takeIf { it.isStringLiteral }?.stringValue
    }

    protected fun tryConvertToNumber(requestedNumberType: Class<*>): Any? {
        val literal = unwrappedElement() as? JSLiteralExpression ?: return null
        val value = literal.takeIf { it.isNumericLiteral }?.value ?: return null

        // currently number can only return BigInteger, Long or Double.
        // com.intellij.lang.javascript.psi.impl.JSLiteralExpressionImpl.getValue
        if (value is BigInteger) {
            return when (requestedNumberType) {
                Int::class.java, java.lang.Integer::class.java -> value.intValueExact()
                Float::class.java, java.lang.Float::class.java -> value.toFloat()
                Long::class.java, java.lang.Long::class.java -> value.toLong()
                Double::class.java, java.lang.Double::class.java -> value.toDouble()
                java.math.BigDecimal::class.java -> value.toBigDecimal()
                java.math.BigInteger::class.java -> value
                else -> null
            }
        }
        if (value is Long) {
            return when (requestedNumberType) {
                Int::class.java, java.lang.Integer::class.java -> value.toInt()
                Float::class.java, java.lang.Float::class.java -> value.toFloat()
                Long::class.java, java.lang.Long::class.java -> value
                Double::class.java, java.lang.Double::class.java -> value.toDouble()
                java.math.BigDecimal::class.java -> value.toBigDecimal()
                java.math.BigInteger::class.java -> value.toBigInteger()
                else -> null
            }
        }
        if (value is Double) {
            return when (requestedNumberType) {
                Int::class.java, java.lang.Integer::class.java -> value.toInt()
                Float::class.java, java.lang.Float::class.java -> value.toFloat()
                Long::class.java, java.lang.Long::class.java -> value.toLong()
                Double::class.java, java.lang.Double::class.java -> value
                java.math.BigDecimal::class.java -> value.toBigDecimal()
                java.math.BigInteger::class.java -> value.toBigDecimal().toBigInteger()
                else -> null
            }
        }
        return null
    }

    protected fun tryConvertToBoolean(): Boolean? {
        val literal = unwrappedElement() as? JSLiteralExpression ?: return null
        return literal.takeIf { it.isBooleanLiteral }?.value as? Boolean
    }

    protected fun tryConvertToObjectLiteral(): IObjectLiteral? {
        val literal = unwrappedElement() as? JSObjectLiteralExpression ?: return null
        return ideaModule.spiFactory.createObjectLiteralExpression(literal).api()
    }
}