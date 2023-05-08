/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i

import com.intellij.analysis.AnalysisScope
import com.intellij.analysis.AnalysisUIOptions
import com.intellij.analysis.BaseAnalysisActionDialog
import com.intellij.analysis.dialog.ModelScopeItem
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.lang.java.JavaLanguage
import com.intellij.lang.properties.psi.PropertiesFile
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessModuleDir
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VfsUtilCore
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
import com.intellij.util.PathUtil
import com.intellij.util.Query
import com.intellij.util.containers.stream
import org.eclipse.scout.sdk.core.java.JavaTypes
import org.eclipse.scout.sdk.core.java.apidef.ITypeNameSupplier
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment
import org.eclipse.scout.sdk.core.java.model.api.IType
import org.eclipse.scout.sdk.core.s.environment.IEnvironment
import org.eclipse.scout.sdk.core.s.environment.IProgress
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi
import org.eclipse.scout.sdk.core.s.nls.ITranslationStore
import org.eclipse.scout.sdk.core.s.nls.Language
import org.eclipse.scout.sdk.core.s.nls.manager.IStackedTranslation
import org.eclipse.scout.sdk.core.s.nls.properties.EditableTranslationFile
import org.eclipse.scout.sdk.core.s.nls.properties.PropertiesTranslationStore
import org.eclipse.scout.sdk.core.s.nls.properties.ReadOnlyTranslationFile
import org.eclipse.scout.sdk.core.util.Strings
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
    val module = javaEnvironment().toIdea().module
    return computeInReadAction(module.project) {
        module.findTypeByName(name())
    }
}

/**
 * @return this [ProgressIndicator] converted to a Scout [IProgress].
 */
fun ProgressIndicator?.toScoutProgress() = IdeaProgress(this)

/**
 * @return this Scout [IProgress] converted to an [IdeaProgress].
 */
fun IProgress?.toIdea() = this as? IdeaProgress ?: IdeaProgress.empty()

/**
 * @return this [IEnvironment] converted to a [IdeaEnvironment].
 */
fun IEnvironment.toIdea() = this as IdeaEnvironment

/**
 * @return this [IJavaEnvironment] converted to an [JavaEnvironmentWithIdea]
 */
fun IJavaEnvironment.toIdea() = unwrap() as JavaEnvironmentWithIdea

/**
 * @return the module source root (source folder) or library source root in which this PsiElement exists.
 */
fun PsiElement.resolveSourceRoot() = containingFile
    ?.virtualFile
    ?.let { ProjectFileIndex.getInstance(project).getSourceRootForFile(it) }

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
    val fqn = computeInReadAction(project) { qualifiedName }
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
 * @param typeParamIndex The zero based index of the type parameter as declared by the class [levelFqn].
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
    val psiFile = containingFile
    val isInProject = psiFile?.virtualFile?.isInLocalFileSystem ?: true /* a psi element which has not a file (e.g. PsiDirectory) */
    if (!returnReferencingModuleIfNotInFilesystem && !isInProject) {
        return null
    }

    val searchElement = if (isInProject) this else psiFile ?: this
    return computeInReadAction(project) {
        this
            .takeIf { it.isValid }
            ?.let { ModuleUtilCore.findModuleForPsiElement(searchElement) }
    }
}

fun PsiElement.scoutApi() = ApiHelper.scoutApiFor(this)

fun PsiElement.requireScoutApi() = ApiHelper.requireScoutApiFor(this)

/**
 * @return true if this [Module] has a Java SDK.
 */
fun Module.isJavaModule(): Boolean = rootManager.sdk?.sdkType == JavaSdk.getInstance()

/**
 * @return all source folders of this [Module].
 */
fun Module.sourceFolders() = ModuleRootManager.getInstance(this).contentEntries.asSequence()
    .flatMap { it.sourceFolders.asSequence() }

/**
 * @param scope The scope in which the PsiClasses should be searched.
 * @param checkDeep true if deep search should be performed.
 * @param includeAnonymous true if anonymous subclasses should be returned as well.
 * @param includeRoot true if the root [PsiClass] should be returned as well.
 * @return all subtypes of this [PsiClass] that fulfill the given filter options.
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
    options.setFileType(JavaFileType.INSTANCE)

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
        .asSequence()
        .filter { it.isValid }

/**
 * Gets the [PsiClass] from the classpath of this [Module] having the give fully qualified name
 * @param fqn The fully qualified name to search
 * @param includeTests If test classpath should be included or not
 * @return the [PsiClass] from the classpath of this [Module] having the give fully qualified name
 */
fun Module.findTypeByName(fqn: String, includeTests: Boolean = true) = project
    .findTypesByName(fqn, moduleWithDependenciesAndLibrariesScope(this, includeTests))
    .firstOrNull()

/**
 * @return The [Module] within the given [Project] in which this file exists.
 */
fun VirtualFile.containingModule(project: Project) = ModuleUtilCore.findModuleForFile(this, project)

/**
 * @return The source folder in which this file is stored. May return null in case the file is not part of a [Module] or is not below a source folder of the module.
 */
fun VirtualFile.containingSourceFolder(project: Project) = containingModule(project)?.sourceFolders()?.firstOrNull {
    val sourceFolderDir = it.file
    return@firstOrNull sourceFolderDir != null && VfsUtilCore.isAncestor(sourceFolderDir, this, false)
}

/**
 * @return The directory [Path] of this [Module]. It is the main content root of the [Module] or the directory containing the .iml file if no content roots are available
 */
fun Module.moduleDirPath(): Path = guessModuleDir()?.resolveLocalPath() ?: Paths.get(ModuleUtilCore.getModuleDirPath(this))

/**
 * Executes the given [IBreadthFirstVisitor] on all super classes. The starting [PsiClass] is visited as well.
 * @param visitor The [IBreadthFirstVisitor] to execute.
 * @return The result from the last call to the visitor.
 */
fun PsiClass.visitSupers(visitor: IBreadthFirstVisitor<PsiClass>): TreeVisitResult {
    val supplier: Function<PsiClass, Stream<out PsiClass>> = Function { a -> a.supers.stream() }
    return TreeTraversals.create(visitor, supplier).traverse(this)
}

fun PsiClass.isInstanceOf(typeNameSupplier: ITypeNameSupplier) = isInstanceOf(typeNameSupplier.fqn())

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

/**
 * Tries to resolve the [PropertiesFile] that contains the text properties for the [Language] given.
 * @param language The [Language] of this [ITranslationStore] for which the [PropertiesFile] should be returned.
 * @param psiManager The [PsiManager] to look up the file.
 * @return The [PropertiesFile] or null if no file could be found for the given [Language].
 */
fun ITranslationStore.resolvePropertiesFile(language: Language, psiManager: PsiManager): PropertiesFile? {
    val store = this as? PropertiesTranslationStore ?: return null
    val file = store.files()[language] ?: return null
    val virtualFile = when (file) {
        is EditableTranslationFile -> file.path().toVirtualFile()
        is ReadOnlyTranslationFile -> file.source() as? VirtualFile
        else -> null
    } ?: return null
    val propertiesFile = psiManager.findFile(virtualFile)
    return propertiesFile as? PropertiesFile
}

/**
 * Tries to resolve the [com.intellij.lang.properties.IProperty] that corresponds to this [IStackedTranslation] and the [Language] given.
 * @param language The [Language] of this [IStackedTranslation] for which the [com.intellij.lang.properties.IProperty] should be returned.
 * @param project The [Project] in which the [com.intellij.lang.properties.IProperty] should be searched.
 * @return The [com.intellij.lang.properties.IProperty] of this [IStackedTranslation] and the [Language] given or null if it could not be found.
 */
fun IStackedTranslation.resolveProperty(language: Language, project: Project) = resolveProperty(language, PsiManager.getInstance(project))

/**
 * Tries to resolve the [com.intellij.lang.properties.IProperty] that corresponds to this [IStackedTranslation] and the [Language] given.
 * @param language The [Language] of this [IStackedTranslation] for which the [com.intellij.lang.properties.IProperty] should be returned.
 * @param psiManager The [PsiManager] to look up the file.
 * @return The [com.intellij.lang.properties.IProperty] of this [IStackedTranslation] and the [Language] given or null if it could not be found.
 */
fun IStackedTranslation.resolveProperty(language: Language, psiManager: PsiManager) = entry(language).orElse(null)
    ?.store()
    ?.resolvePropertiesFile(language, psiManager)
    ?.findPropertyByKey(key())

/**
 * Tries to resolve the local [Path] of this [VirtualFile] if this [VirtualFile] points to such a file.
 * If it cannot be resolved (e.g. because it is a file in a zip or jar), null is returned.
 * @return The local [Path] of this [VirtualFile] or null if it cannot be computed.
 * @throws java.nio.file.InvalidPathException if the location of this [VirtualFile] cannot be converted to a [Path]
 */
fun VirtualFile.resolveLocalPath() = PathUtil.getLocalPath(this)?.let { Paths.get(it) }

/**
 * Gets the content of this [VirtualFile] as [java.lang.StringBuilder].
 *
 * The StringBuilder does not contain a BOM if there is any in the [VirtualFile].
 * See [Unicode Byte Order Mark FAQ](https://unicode.org/faq/utf_bom.html) for an explanation.
 *
 * @return The content of this [VirtualFile] as [java.lang.StringBuilder]
 * @throws java.io.IOException if an I/O error occurs
 * @see [VirtualFile.contentsToByteArray]
 */
fun VirtualFile.contentAsText(): StringBuilder = inputStream.use {
    Strings.fromInputStream(it, charset, length.toInt())
}

/**
 * Starts a Scope selection dialog ([BaseAnalysisActionDialog]) based on this [AnActionEvent].
 * @return The [AnalysisScope] selected by the user or null of the user canceled the dialog.
 */
fun AnActionEvent.chooseAnalysisScope(title: String, analysisNoon: String, acceptScopeItem: (ModelScopeItem) -> Boolean = { _ -> true }): AnalysisScope? {
    val data = DataContextHelper(dataContext)
    val project = data.project() ?: return null
    val initialAnalysisScope = data.scope() ?: return null

    val items = BaseAnalysisActionDialog.standardItems(project, initialAnalysisScope, data.module(), data.psiElement())
        .filter { acceptScopeItem(it) }
    val options = AnalysisUIOptions.getInstance(project)
    val dialog = BaseAnalysisActionDialog(title, analysisNoon, project, items, options, true)
    if (dialog.showAndGet()) {
        return dialog.getScope(initialAnalysisScope)
    }
    return null
}