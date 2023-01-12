/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.derived

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiModifier
import com.intellij.psi.search.SearchScope
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi
import org.eclipse.scout.sdk.core.s.apidef.ScoutApi
import org.eclipse.scout.sdk.core.s.derived.DtoUpdateHandler
import org.eclipse.scout.sdk.core.s.derived.IDerivedResourceHandler
import org.eclipse.scout.sdk.s2i.findAllTypesAnnotatedWith
import kotlin.streams.asSequence


open class DtoUpdateHandlerFactory : DerivedResourceHandlerFactory {
    override fun createHandlersFor(scope: SearchScope, project: Project) =
            ScoutApi.allKnown().asSequence()
                    .flatMap { dtoMarkerAnnotationNames(it) }
                    .distinct()
                    .flatMap { project.findAllTypesAnnotatedWith(it, scope) }
                    .filter(this::acceptClass)
                    .flatMap(this::typeToHandlers)

    protected fun dtoMarkerAnnotationNames(scoutApi: IScoutApi) = sequenceOf(scoutApi.FormData().fqn(), scoutApi.PageData().fqn(), scoutApi.Data().fqn())

    fun typeToHandlers(type: PsiClass): Sequence<IDerivedResourceHandler> = sequenceOf(DtoUpdateHandler(DerivedResourceInputWithIdea(type)))

    protected fun acceptClass(type: PsiClass): Boolean = type.isValid
            && type.isPhysical
            && type.isWritable
            && !type.isInterface
            && !type.isAnnotationType
            && !type.isEnum
            && type.hasModifierProperty(PsiModifier.PUBLIC)
            && type.scope is PsiJavaFile // primary type
}