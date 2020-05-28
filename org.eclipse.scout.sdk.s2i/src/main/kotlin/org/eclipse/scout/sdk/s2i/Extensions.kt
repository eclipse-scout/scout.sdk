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
package org.eclipse.scout.sdk.s2i

import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.intellij.structuralsearch.*
import com.intellij.structuralsearch.plugin.util.CollectingMatchResultSink
import com.intellij.util.CollectionQuery
import com.intellij.util.Query
import com.intellij.util.containers.stream
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.log.SdkLog.onTrace
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment
import org.eclipse.scout.sdk.core.model.api.IType
import org.eclipse.scout.sdk.core.s.environment.IEnvironment
import org.eclipse.scout.sdk.core.s.environment.IProgress
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.core.util.SdkException
import org.eclipse.scout.sdk.core.util.visitor.IBreadthFirstVisitor
import org.eclipse.scout.sdk.core.util.visitor.TreeTraversals
import org.eclipse.scout.sdk.core.util.visitor.TreeVisitResult
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.computeInReadAction
import org.eclipse.scout.sdk.s2i.environment.IdeaProgress
import org.eclipse.scout.sdk.s2i.environment.model.JavaEnvironmentWithIdea
import java.lang.reflect.InvocationTargetException
import java.nio.file.Path
import java.nio.file.Paths
import java.util.function.Function
import java.util.stream.Stream

private val useLegacyMatcher: FinalValue<Boolean> = FinalValue()

fun IType.resolvePsi(): PsiClass? {
    val module = this.javaEnvironment().toIdea().module
    return computeInReadAction(module.project) {
        module.project
                .findTypesByName(name(), GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module, true))
                .firstOrNull()
    }
}

fun ProgressIndicator.toScoutProgress(): IdeaProgress = IdeaProgress(this)

fun PsiElement.resolveSourceRoot(): VirtualFile? {
    return this.containingFile
            ?.virtualFile
            ?.let { ProjectFileIndex.getInstance(this.project).getSourceRootForFile(it) }
}

/**
 * Converts this [PsiClass] into its corresponding Scout [IType].
 *
 *  If the [PsiClass] is within a file in the [Project] (part of the project sources), the classpath of the parent module is used to resolve the Scout [IType].
 *
 * If not in the [Project] files and [returnReferencingModuleIfNotInFilesystem] is true, the [PsiClass] will be resolved based on the classpath of a [Module] that contains this [PsiClass]. It is undefined which [Module] exactly that is used.
 *
 * If not in the [Project] files and [returnReferencingModuleIfNotInFilesystem] is false, null is returned (no attempt is performed to resolve the [PsiClass]).
 *
 * @param returnReferencingModuleIfNotInFilesystem specifies how to handle [PsiClass]es which are not part of the [Project] files (see above). The default is true.
 *
 * @return The [IType] corresponding to this [PsiClass].
 */
fun PsiClass.toScoutType(env: IdeaEnvironment, returnReferencingModuleIfNotInFilesystem: Boolean = true): IType? =
        containingModule(returnReferencingModuleIfNotInFilesystem)
                ?.let { env.toScoutJavaEnvironment(it) }
                ?.let { toScoutType(it) }

fun PsiClass.toScoutType(env: IJavaEnvironment): IType? {
    val fqn = computeInReadAction(this.project) { this.qualifiedName }
    return env.findType(fqn).orElse(null)
}

/**
 * Gets the [Module] of the receiver.
 * @param returnReferencingModuleIfNotInFilesystem specifies how to handle [PsiElement]s which are not part of the [Project] files (e.g. exist in a library).
 * If true, querying the [Module] for such an element will return an instance that includes the [PsiElement] in its classpath.
 * If false only files in the [Project] will return a [Module].
 * @return The [Module] in which this [PsiElement] exists.
 *
 */
fun PsiElement.containingModule(returnReferencingModuleIfNotInFilesystem: Boolean = true): Module? {
    val isInProject = containingFile?.virtualFile?.isInLocalFileSystem ?: true /* a psi element which has not a file (e.g. PsiDirectory) */
    if (!returnReferencingModuleIfNotInFilesystem && !isInProject) {
        return null
    }

    val searchElement = if (isInProject) this else containingFile ?: this
    return computeInReadAction(this.project) {
        this
                .takeIf { it.isValid }
                ?.let { ModuleUtil.findModuleForPsiElement(searchElement) }
    }
}

fun IProgress.toIdea(): IdeaProgress = this as IdeaProgress

fun Module.isJavaModule(): Boolean = ModuleRootManager.getInstance(this).sdk?.sdkType == JavaSdk.getInstance()

fun IEnvironment.toIdea(): IdeaEnvironment = this as IdeaEnvironment

fun IJavaEnvironment.toIdea(): JavaEnvironmentWithIdea = this.unwrap() as JavaEnvironmentWithIdea

fun PsiClass.newSubTypeHierarchy(scope: SearchScope, checkDeep: Boolean, includeAnonymous: Boolean, includeRoot: Boolean): Query<PsiClass> {
    val children = ClassInheritorsSearch.search(this, scope, checkDeep, true, includeAnonymous)
    if (!includeRoot) {
        return children
    }

    val resultWithRoot = children.findAll()
    resultWithRoot.add(this)
    return CollectionQuery(resultWithRoot)
}

fun Path.toVirtualFile() = VfsUtil.findFile(this, true)
        ?.takeIf { it.isValid }

fun Project.findAllTypesAnnotatedWith(annotation: String, scope: SearchScope) = findAllTypesAnnotatedWith(annotation, scope, null)

fun Project.findAllTypesAnnotatedWith(annotation: String, scope: SearchScope, indicator: ProgressIndicator?): Sequence<PsiClass> {
    val options = MatchOptions()
    options.dialect = JavaLanguage.INSTANCE
    options.isCaseSensitiveMatch = true
    options.isRecursiveSearch = true
    options.scope = scope
    options.searchPattern = "@$annotation( )\nclass \$Class\$ {}"

    val constraint = MatchVariableConstraint()
    constraint.name = "Class"
    options.addVariableConstraint(constraint)

    return structuralSearch(options, indicator)
            .map { it.match }
            .filter { it.isValid }
            .filter { it.isPhysical }
            .filter { it is PsiClass }
            .map { it as PsiClass }
}

fun Project.structuralSearch(query: MatchOptions, indicator: ProgressIndicator?): Sequence<MatchResult> {
    val progress = indicator ?: EmptyProgressIndicator()
    val result = object : CollectingMatchResultSink() {
        override fun getProgressIndicator(): ProgressIndicator {
            return progress
        }
    }
    findMatches(Matcher(this, query), result, query)
    return result.matches.asSequence()
}

private fun findMatches(matcher: Matcher, result: MatchResultSink, options: MatchOptions) {
    // sample taken from com.intellij.structuralsearch.plugin.ui.SearchCommand
    // the API is different in IntelliJ 19x than in 20x
    try {
        if (useLegacyMatcher.computeIfAbsentAndGet { isUseLegacyMatcher() }) {
            findMatchesLegacy(matcher, result, options)
        } else {
            findMatchesNew(matcher, result)
        }
    } catch (e: InvocationTargetException) {
        throw expandInvocationTargetException(e)
    }
}

private fun expandInvocationTargetException(e: InvocationTargetException): RuntimeException {
    var original: Throwable? = e
    while (original is InvocationTargetException) {
        original = e.cause
    }
    if (original is RuntimeException) {
        return original
    }
    return SdkException(original)
}

// Can be removed if the supported min. IJ version is 2020.1
private fun isUseLegacyMatcher() =
        try {
            findMatchesMethodNew()
            false
        } catch (e: NoSuchMethodException) {
            SdkLog.debug("Using legacy structural search API", onTrace(e))
            true
        }

private fun findMatchesMethodNew() = Matcher::class.java.getMethod("findMatches", MatchResultSink::class.java)

private fun findMatchesMethodLegacy() = Matcher::class.java.getMethod("findMatches", MatchResultSink::class.java, MatchOptions::class.java)

private fun findMatchesNew(matcher: Matcher, result: MatchResultSink) = findMatchesMethodNew().invoke(matcher, result)

private fun findMatchesLegacy(matcher: Matcher, result: MatchResultSink, options: MatchOptions) = findMatchesMethodLegacy().invoke(matcher, result, options)

fun Project.findTypesByName(fqn: String) = findTypesByName(fqn, GlobalSearchScope.allScope(this))

fun Project.findTypesByName(fqn: String, scope: GlobalSearchScope) =
        computeInReadAction(this) {
            JavaPsiFacade.getInstance(this)
                    .findClasses(fqn, scope)
                    .toSet()
        }
                .filter { it.isValid }

/**
 * @return A [Path] representing this [VirtualFile].
 */
fun VirtualFile.getNioPath(): Path = VfsUtilCore.virtualToIoFile(this).toPath() // don't use toNioPath as method name because this name already exists in VirtualFile since IJ 2020.2

fun VirtualFile.containingModule(project: Project) = ProjectFileIndex.getInstance(project).getModuleForFile(this)

fun Module.moduleDirPath(): Path = Paths.get(ModuleUtil.getModuleDirPath(this))

fun PsiClass.visitSupers(visitor: IBreadthFirstVisitor<PsiClass>): TreeVisitResult {
    val supplier: Function<PsiClass, Stream<out PsiClass>> = Function { a -> a.supers.stream() }
    return TreeTraversals.create(visitor, supplier).traverse(this)
}

fun PsiClass.isInstanceOf(vararg parentFqn: String): Boolean =
        computeInReadAction(project) {
            visitSupers(IBreadthFirstVisitor { element, _, _ ->
                if (parentFqn.contains(element.qualifiedName))
                    TreeVisitResult.TERMINATE
                else
                    TreeVisitResult.CONTINUE
            }) == TreeVisitResult.TERMINATE
        }
