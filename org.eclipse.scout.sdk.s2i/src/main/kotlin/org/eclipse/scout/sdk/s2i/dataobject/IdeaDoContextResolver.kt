/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.dataobject

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiModifier
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment
import org.eclipse.scout.sdk.core.java.model.api.IType
import org.eclipse.scout.sdk.core.s.dataobject.DoContextResolvers
import org.eclipse.scout.sdk.core.s.java.apidef.IScout22DoApi
import org.eclipse.scout.sdk.core.s.java.apidef.ScoutApi
import org.eclipse.scout.sdk.s2i.*
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.computeInReadAction
import java.util.stream.Stream
import kotlin.streams.asSequence
import kotlin.streams.asStream

open class IdeaDoContextResolver : DoContextResolvers.IDoContextResolver, StartupActivity, DumbAware {

    override fun runActivity(project: Project) {
        DoContextResolvers.set(this)
    }

    override fun resolveNamespaceCandidates(environment: IJavaEnvironment) = resolveNamespaceCandidates(environment.toIdea().module, environment)

    fun resolveNamespaceCandidates(module: Module, environment: IJavaEnvironment) = computeInReadAction(module.project) {
        val moduleScope = module.getModuleWithDependenciesAndLibrariesScope(false)
        return@computeInReadAction ScoutApi.allKnown().asSequence()
                .mapNotNull { it.api(IScout22DoApi::class.java).orElse(null) }
                .map { it.INamespace().fqn() }
                .distinct()
                .flatMap { module.project.findTypesByName(it) }
                .flatMap { it.newSubTypeHierarchy(moduleScope, checkDeep = true, includeAnonymous = false, includeRoot = false).asSequence() }
                .filter { it.isValid && it.isPhysical && it.isWritable }
                .filter { !it.isEnum && !it.isInterface && !it.isAnnotationType }
                .filter { !it.hasModifierProperty(PsiModifier.ABSTRACT) }
                .map { it.toScoutType(environment) }
                .filterNotNull()
                .asStream()
    }

    override fun resolvePrimaryTypesInPackageOf(ref: IType?): Stream<IType> {
        val javaEnvironment = ref?.javaEnvironment() ?: return Stream.empty()
        val project = javaEnvironment.toIdea().module.project
        return computeInReadAction(project) {
            val psi = ref.resolvePsi() ?: return@computeInReadAction Stream.empty()
            val javaFile = psi.containingFile as? PsiJavaFile ?: return@computeInReadAction Stream.empty()
            val psiPackage = JavaPsiFacade.getInstance(project).findPackage(javaFile.packageName) ?: return@computeInReadAction Stream.empty()
            return@computeInReadAction psiPackage.classes.asSequence()
                    .mapNotNull { it.toScoutType(javaEnvironment) }
                    .asStream()
        }
    }
}