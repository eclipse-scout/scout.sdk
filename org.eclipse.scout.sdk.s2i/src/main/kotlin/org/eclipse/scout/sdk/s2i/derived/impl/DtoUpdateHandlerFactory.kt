/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i.derived.impl

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiModifier
import com.intellij.psi.search.SearchScope
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes
import org.eclipse.scout.sdk.core.s.derived.DtoUpdateHandler
import org.eclipse.scout.sdk.core.s.environment.IEnvironment
import org.eclipse.scout.sdk.core.s.environment.IFuture
import org.eclipse.scout.sdk.core.s.environment.IProgress
import org.eclipse.scout.sdk.s2i.derived.DerivedResourceHandlerFactory
import org.eclipse.scout.sdk.s2i.findAllTypesAnnotatedWith
import java.util.function.BiFunction


open class DtoUpdateHandlerFactory : DerivedResourceHandlerFactory {
    override fun createHandlersFor(scope: SearchScope, project: Project): Sequence<BiFunction<IEnvironment, IProgress, Collection<IFuture<*>>>> {
        val elements = project.findAllTypesAnnotatedWith(IScoutRuntimeTypes.Data, scope) +
                project.findAllTypesAnnotatedWith(IScoutRuntimeTypes.FormData, scope) +
                project.findAllTypesAnnotatedWith(IScoutRuntimeTypes.PageData, scope)

        return elements
                .filter(this::acceptClass)
                .flatMap(this::typeToHandlers)
    }

    fun typeToHandlers(type: PsiClass): Sequence<BiFunction<IEnvironment, IProgress, Collection<IFuture<*>>>> =
            sequenceOf(DtoUpdateHandler(DerivedResourceInputWithIdea(type)))

    protected fun acceptClass(type: PsiClass): Boolean = type.isValid
            && type.isPhysical
            && type.isWritable
            && !type.isInterface
            && !type.isAnnotationType
            && !type.isEnum
            && type.hasModifierProperty(PsiModifier.PUBLIC)
            && type.canNavigateToSource()
            && type.scope is PsiJavaFile // primary type
}