/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i.model.js

import com.intellij.lang.javascript.inspections.JSRecursiveWalkingElementSkippingNestedFunctionsVisitor
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.psi.PsiElement
import org.eclipse.scout.sdk.core.log.SdkLog

/**
 * Represents a JavaScript class.
 */
class JsModelClass(name: String, jsClass: JSClass, scoutJsModule: JsModule, constructor: JSFunction?, init: JSFunction?) : AbstractJsModelElement(name, scoutJsModule) {

    /**
     * The qualified super class names
     */
    val superClassNames: List<String>

    init {
        superClassNames = getQualifiedSuperClassNames(jsClass, scoutJsModule)
        properties = parseProperties(jsClass, constructor, init)
    }

    companion object {

        private const val CONSTRUCTOR_METHOD_NAME = "constructor"
        private const val INIT_METHOD_NAME = "_init"
        private const val WIDGET_PROPERTIES_METHOD_PART = "WidgetProperties" // "_addWidgetProperties" method
        private const val TEXT_KEYS_METHOD_PART = "TextKeys" // "resolveTextKeys" method

        fun parse(jsClass: JSClass, scoutJsModule: JsModule): JsModelClass? {
            val className = jsClass.name ?: return null
            if (isPrivateOrJQueryLikeName(className)) return null
            val constructor = findMethodIn(jsClass, CONSTRUCTOR_METHOD_NAME)
            val init = findMethodIn(jsClass, INIT_METHOD_NAME)
            return JsModelClass(className, jsClass, scoutJsModule, constructor, init)
        }

        private fun parseMethodCallWithStringArgumentsIn(function: PsiElement, methodNameSubstring: String): Set<String> {
            val stringLiterals = LinkedHashSet<String>()
            function.accept(object : JSRecursiveWalkingElementVisitor() {
                override fun visitJSCallExpression(node: JSCallExpression) {
                    super.visitJSCallExpression(node)
                    stringLiterals.addAll(parseMethodCallWithStringArguments(node, methodNameSubstring))
                }
            })
            return stringLiterals
        }

        private fun parseMethodCallWithStringArguments(methodCall: JSCallExpression, methodNameSubstring: String) = methodCall
                .takeIf { methodCall.methodExpression?.text?.contains(methodNameSubstring, true) == true }
                ?.argumentList
                ?.let { parseStringLiteralsFromArgumentList(it) }
                ?: emptySet()

        private fun parseStringLiteralsFromArgumentList(argumentList: JSArgumentList): Set<String> {
            val literalValues = LinkedHashSet<String>()
            argumentList.accept(object : JSRecursiveWalkingElementSkippingNestedFunctionsVisitor() {
                override fun visitJSLiteralExpression(node: JSLiteralExpression) {
                    super.visitJSLiteralExpression(node)
                    node
                            .takeIf { it.isStringLiteral }
                            ?.stringValue
                            ?.let { literalValues.add(it) }
                }
            })
            return literalValues
        }

        private fun findMethodIn(element: PsiElement, methodName: String): JSFunction? {
            var result: JSFunction? = null
            element.acceptChildren(object : JSElementVisitor() {
                override fun visitJSFunctionDeclaration(node: JSFunction) {
                    if (methodName == node.name) {
                        result = node
                    }
                }
            })
            return result
        }
    }

    private fun getQualifiedSuperClassNames(jsClass: JSClass, scoutJsModule: JsModule) = jsClass.superClasses.mapNotNull { superClass ->
        val superClassName = superClass.name ?: return@mapNotNull null
        val superClassFile = superClass.containingFile.virtualFile
        val superClassModule = scoutJsModule.jsModel.containingModule(superClassFile)
        val namespace = superClassModule?.namespace ?: return@mapNotNull superClassName
        "$namespace.$superClassName"
    }

    private fun parseProperties(jsClass: JSClass, constructor: JSFunction?, init: JSFunction?): List<JsModelProperty> {
        if (constructor == null) return emptyList()
        val widgetProperties = parseMethodCallWithStringArgumentsIn(constructor, WIDGET_PROPERTIES_METHOD_PART)
        val translationProperties = init?.let { parseMethodCallWithStringArgumentsIn(it, TEXT_KEYS_METHOD_PART) } ?: emptySet()
        val specialPropertyTypes = listOf(
                JsModelPropertyRecorder(widgetProperties, JsModelProperty.JsPropertyDataType.WIDGET, this),
                JsModelPropertyRecorder(translationProperties, JsModelProperty.JsPropertyDataType.TEXT_KEY, this))

        val result = ArrayList<JsModelProperty>()
        constructor.accept(object : JSRecursiveWalkingElementVisitor() {
            override fun visitJSAssignmentExpression(node: JSAssignmentExpression) {
                super.visitJSAssignmentExpression(node)
                JsModelProperty.parse(node, this@JsModelClass, specialPropertyTypes)?.let { result.add(it) }
            }
        })

        // register properties that have no initializer but are declared as widget or translation properties
        // in that case it is unknown if it is an array or not but it is known that the property exists. it is assumed it is no array
        result.addAll(specialPropertyTypes
                .flatMap { it.unused() }
                .onEach { SdkLog.debug("Property {}.{} is declared as {} property but is not initialized in the constructor.", jsClass.name, it.name, it.dataType.type) })
        return result
    }

    class JsModelPropertyRecorder(private val propertyNames: Collection<String>, private val type: JsModelProperty.JsPropertyDataType, private val owner: AbstractJsModelElement) {
        private val m_unused = HashSet(propertyNames)
        fun use(name: String, isArray: Boolean) = name.takeIf { propertyNames.contains(name) }?.let {
            m_unused.remove(name) // mark as used
            JsModelProperty(name, owner, type, isArray)
        }

        fun unused() = m_unused
                .filter { !JsModelProperty.isInternalProperty(owner.qualifiedName(), it) }
                .map { JsModelProperty(it, owner, type, false) }
    }
}