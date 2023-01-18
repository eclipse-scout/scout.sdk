/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.template.java

import com.intellij.codeInsight.completion.JavaPsiClassReferenceElement
import com.intellij.codeInsight.template.*
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiModifier
import com.intellij.psi.search.GlobalSearchScope.moduleWithDependenciesAndLibrariesScope
import org.eclipse.scout.sdk.s2i.containingModule
import org.eclipse.scout.sdk.s2i.findTypeByName
import org.eclipse.scout.sdk.s2i.newSubTypeHierarchy

/**
 * Macro providing all abstract subclasses of a root class.
 */
class DescendantAbstractClassesEnumMacro : Macro() {

    companion object {
        const val NAME = "descendantAbstractClassesEnum"
    }

    override fun getName() = NAME

    override fun getPresentableName() = "$NAME(String)"

    override fun calculateResult(params: Array<out Expression>, context: ExpressionContext?) =
            findDescendant(params, context)
                    ?.let { PsiClassResult(it) }

    override fun calculateLookupItems(params: Array<out Expression>, context: ExpressionContext?) =
            findDescendants(params, context)
                    ?.map { JavaPsiClassReferenceElement(it) }
                    ?.toTypedArray()

    override fun isAcceptableInContext(context: TemplateContextType?) = context is JavaCodeContextType

    private fun findDescendant(params: Array<out Expression>, context: ExpressionContext?) =
            MacroArguments(params, context).resolveDefault()
                    ?: findDescendants(params, context)?.firstOrNull()

    private fun findDescendants(params: Array<out Expression>, context: ExpressionContext?): List<PsiClass>? {
        val args = MacroArguments(params, context)
        val baseFqn = args.baseFqn ?: return null
        val module = args.module ?: return null
        val baseClass = module.findTypeByName(baseFqn) ?: return null
        val descendants = baseClass.newSubTypeHierarchy(moduleWithDependenciesAndLibrariesScope(module, true), checkDeep = true, includeAnonymous = false, includeRoot = false)
                .filter { !it.isInterface }
                .filter { it.hasModifierProperty(PsiModifier.ABSTRACT) }
                .filter { it.qualifiedName != args.defaultFqn }

        // if a default is given: ensure it is the first item in the list
        return args.resolveDefault()
                ?.let { listOf(it) + descendants } ?: descendants
    }

    open class PsiClassResult(psiClass: PsiClass) : PsiExpressionEnumMacro.PsiElementResult(psiClass, psiClass.qualifiedName ?: "")

    private class MacroArguments(params: Array<out Expression>, context: ExpressionContext?) {
        val baseFqn = params.firstOrNull()?.calculateResult(context)?.toString()
        val defaultFqn = if (params.size > 1) params[1].calculateResult(context)?.toString() else null
        val module = context?.psiElementAtStartOffset?.containingModule()

        fun resolveDefault() = defaultFqn?.let { module?.findTypeByName(it) }
    }
}