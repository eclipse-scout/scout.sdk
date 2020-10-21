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
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.GlobalSearchScope.moduleWithDependenciesAndLibrariesScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.intellij.psi.util.InheritanceUtil
import com.intellij.psi.util.PsiUtil
import com.intellij.structuralsearch.MatchOptions
import com.intellij.structuralsearch.MatchResult
import com.intellij.structuralsearch.MatchVariableConstraint
import com.intellij.structuralsearch.Matcher
import com.intellij.structuralsearch.plugin.util.CollectingMatchResultSink
import com.intellij.util.CollectionQuery
import com.intellij.util.Query
import com.intellij.util.containers.stream
import org.eclipse.scout.sdk.core.apidef.IClassNameSupplier
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment
import org.eclipse.scout.sdk.core.model.api.IType
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi
import org.eclipse.scout.sdk.core.s.environment.IEnvironment
import org.eclipse.scout.sdk.core.s.environment.IProgress
import org.eclipse.scout.sdk.core.util.JavaTypes
import org.eclipse.scout.sdk.core.util.visitor.IBreadthFirstVisitor
import org.eclipse.scout.sdk.core.util.visitor.TreeTraversals
import org.eclipse.scout.sdk.core.util.visitor.TreeVisitResult
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.computeInReadAction
import org.eclipse.scout.sdk.s2i.environment.IdeaProgress
import org.eclipse.scout.sdk.s2i.environment.model.JavaEnvironmentWithIdea
import org.eclipse.scout.sdk.s2i.util.ApiHelper
import java.nio.file.Path
import java.nio.file.Paths
import java.util.function.Function
import java.util.stream.Stream

/**
 * @return The [PsiClass] corresponding to this [IType].
 */
fun IType.resolvePsi(): PsiClass? {
    val module = this.javaEnvironment().toIdea().module
    return computeInReadAction(module.project) {
        module.findTypeByName(name())
    }
}

fun ProgressIndicator.toScoutProgress(): IdeaProgress = IdeaProgress(this)

/**
 * @return the module source root (source folder) or library source root in which this PsiElement exists.
 */
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

/**
 * @param env The [IJavaEnvironment] in which the type should be searched.
 * @return The [IType] within the given [IJavaEnvironment] that corresponds to this [PsiClass]
 */
fun PsiClass.toScoutType(env: IJavaEnvironment): IType? {
    val fqn = computeInReadAction(this.project) { this.qualifiedName }
    return env.findType(fqn).orElse(null)
}

/**
 * Tries to find a declaring [PsiClass] which is instanceof the given [typeFilterFqn].
 * @param typeFilterFqn The filter condition. The first declaring class which has a type with this fully qualified name in its super hierarchy is returned.
 * @param scoutApi The [IScoutApi] of this [PsiElement]
 * @param acceptExtensionOwner Specifies how to handle IExtensions: if true and a declaring class is an extension, the extension owner is evaluated as well.
 * If it fulfills the type filter it is returned. The default is false.
 * @return The first [PsiClass] in the declaring classes of this [PsiElement] that fulfills the given type filter honoring extension owners if requested.
 */
fun PsiElement.findEnclosingClass(typeFilterFqn: String, scoutApi: IScoutApi, acceptExtensionOwner: Boolean = false): PsiClass? {
    var place: PsiElement = this
    val module = containingModule()
    while (place !is PsiFile) {
        if (place is PsiClass) {
            if (place.isInstanceOf(typeFilterFqn)) {
                return place
            }
            if (acceptExtensionOwner) {
                val iExtension = scoutApi.IExtension()
                if (place.isInstanceOf(iExtension)) {
                    val extensionOwner = place.resolveTypeArgument(iExtension.ownerTypeParamIndex(), iExtension.fqn())
                            ?.getCanonicalText(false)
                            ?.let { module?.findTypeByName(it) }
                    if (extensionOwner != null && extensionOwner.isInstanceOf(typeFilterFqn)) {
                        return extensionOwner
                    }
                }
            }
        }
        place = place.parent
    }
    return null
}

/**
 * Gets the [PsiType] of the type argument which is declared by the [levelFqn] given at the [typeParamIndex] given.
 * @param typeParamIndex The zero based index of the type parameter as declared by the the class [levelFqn].
 * @param levelFqn The fully qualified name of the class declaring the type parameter. This class must be a super type of this [PsiClass].
 * @return The [PsiType] of the type parameter specified in the context of this [PsiClass].
 */
fun PsiClass.resolveTypeArgument(typeParamIndex: Int, levelFqn: String): PsiType? {
    var result: PsiType? = null
    for (superType in superTypes) {
        InheritanceUtil.processSuperTypes(superType, true) {
            if (levelFqn == JavaTypes.erasure(it.getCanonicalText(false))) {
                result = it.resolveTypeArgument(typeParamIndex)
                return@processSuperTypes false
            }
            return@processSuperTypes true
        }
    }
    return result
}

/**
 * Resolves the type argument of this [PsiType] having the given index.
 * @param index The zero based index of the type argument to return.
 * @return the [PsiType] of the type argument with the given index or null if no such type argument exists.
 */
fun PsiType.resolveTypeArgument(index: Int): PsiType? {
    val resolveResult = PsiUtil.resolveGenericsClassInType(this)
    if (!resolveResult.isValidResult) {
        return null
    }
    return resolveResult.substitutor.substitutionMap.entries
            .filter { it.key.index == index }
            .map { it.value }
            .firstOrNull()
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

fun PsiElement.scoutApi() = ApiHelper.scoutApiFor(this)

fun PsiElement.requireScoutApi() = ApiHelper.requireScoutApiFor(this)

fun IProgress.toIdea(): IdeaProgress = this as IdeaProgress

/**
 * @return true if this [Module] has a Java SDK.
 */
fun Module.isJavaModule(): Boolean = ModuleRootManager.getInstance(this).sdk?.sdkType == JavaSdk.getInstance()

fun IEnvironment.toIdea(): IdeaEnvironment = this as IdeaEnvironment

fun IJavaEnvironment.toIdea(): JavaEnvironmentWithIdea = this.unwrap() as JavaEnvironmentWithIdea

/**
 * @param scope The scope in which the PsiClasses should be searched.
 * @param checkDeep true if deep search should be performed.
 * @param includeAnonymous true if anonymous sub classes should be returned as well.
 * @param includeRoot true if the root [PsiClass] should be returned as well.
 * @return all sub types of this [PsiClass] that fulfill the given filter options.
 */
fun PsiClass.newSubTypeHierarchy(scope: SearchScope, checkDeep: Boolean, includeAnonymous: Boolean, includeRoot: Boolean): Query<PsiClass> {
    val children = ClassInheritorsSearch.search(this, scope, checkDeep, true, includeAnonymous)
    if (!includeRoot) {
        return children
    }

    val resultWithRoot = children.findAll()
    resultWithRoot.add(this)
    return CollectionQuery(resultWithRoot)
}

/**
 * @return The [VirtualFile] that corresponds to this [Path].
 */
fun Path.toVirtualFile() = VfsUtil.findFile(this, true)
        ?.takeIf { it.isValid }

/**
 * @param annotation The fully qualified name of the annotation the class must have.
 * @param scope The scope in which the classes should be searched.
 * @param indicator An optional indicator to report progress.
 * @return All PsiClasses within the given [scope] that have an annotation with the given fully qualified name.
 */
fun Project.findAllTypesAnnotatedWith(annotation: String, scope: SearchScope, indicator: ProgressIndicator? = null): Sequence<PsiClass> {
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

/**
 * Performs the given structural search query.
 * @param query The search to execute
 * @param indicator An optional indicator to report progress
 * @return A [MatchResult] sequence
 */
fun Project.structuralSearch(query: MatchOptions, indicator: ProgressIndicator?): Sequence<MatchResult> {
    val progress = indicator ?: EmptyProgressIndicator()
    val result = object : CollectingMatchResultSink() {
        override fun getProgressIndicator(): ProgressIndicator {
            return progress
        }
    }
    Matcher(this, query).findMatches(result)
    return result.matches.asSequence()
}

/**
 * Finds all PsiClasses in this project having the given fully qualified name.
 * @param fqn The fully qualified name to search
 * @return A [Set] with all [PsiClass] instances having the given fully qualified name.
 */
fun Project.findTypesByName(fqn: String) = findTypesByName(fqn, GlobalSearchScope.allScope(this))

/**
 * Gets all [PsiClass] instances in the [GlobalSearchScope] given with the fully qualified name specified.
 * @param fqn The fully qualified name to search
 * @param scope The scope filter
 * @return all PsiClasses within the given [GlobalSearchScope] having the given fully qualified name.
 */
fun Project.findTypesByName(fqn: String, scope: GlobalSearchScope) =
        computeInReadAction(this) { JavaPsiFacade.getInstance(this).findClasses(fqn, scope) }
                .filter { it.isValid }
                .toSet()

/**
 * Gets the [PsiClass] from the classpath of this [Module] having the give fully qualified name
 * @param fqn The fully qualified name to search
 * @return the [PsiClass] from the classpath of this [Module] having the give fully qualified name
 */
fun Module.findTypeByName(fqn: String) = project.findTypesByName(fqn, moduleWithDependenciesAndLibrariesScope(this, true)).firstOrNull()

/**
 * @return The [Module] within the given [Project] in which this file exists.
 */
fun VirtualFile.containingModule(project: Project) = ProjectFileIndex.getInstance(project).getModuleForFile(this)

/**
 * @return The directory [Path] of this [Module].
 */
fun Module.moduleDirPath(): Path = Paths.get(ModuleUtil.getModuleDirPath(this))

/**
 * Executes the given [IBreadthFirstVisitor] on all super classes. The starting [PsiClass] is visited as well.
 * @param visitor The [IBreadthFirstVisitor] to execute.
 * @return The result from the last call to the visitor.
 */
fun PsiClass.visitSupers(visitor: IBreadthFirstVisitor<PsiClass>): TreeVisitResult {
    val supplier: Function<PsiClass, Stream<out PsiClass>> = Function { a -> a.supers.stream() }
    return TreeTraversals.create(visitor, supplier).traverse(this)
}

fun PsiClass.isInstanceOf(classNameSupplier: IClassNameSupplier) = isInstanceOf(classNameSupplier.fqn())

/**
 * Checks if this [PsiClass] has at least one of the given fully qualified names in its super hierarchy.
 * @param parentFqn The fully qualified names to check
 * @return true if this [PsiClass] is instanceof at least one of the names given.
 */
fun PsiClass.isInstanceOf(vararg parentFqn: String): Boolean = computeInReadAction(project) {
    val visitor: IBreadthFirstVisitor<PsiClass> = IBreadthFirstVisitor { element, _, _ ->
        if (parentFqn.contains(element.qualifiedName))
            TreeVisitResult.TERMINATE
        else
            TreeVisitResult.CONTINUE
    }
    visitSupers(visitor) == TreeVisitResult.TERMINATE
}