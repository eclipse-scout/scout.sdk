package org.eclipse.scout.sdk.s2i

import com.intellij.openapi.fileTypes.StdFileTypes
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.intellij.structuralsearch.MatchOptions
import com.intellij.structuralsearch.MatchVariableConstraint
import com.intellij.structuralsearch.Matcher
import com.intellij.structuralsearch.plugin.util.CollectingMatchResultSink
import com.intellij.util.CollectionQuery
import com.intellij.util.Query
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment
import org.eclipse.scout.sdk.core.model.api.IType
import org.eclipse.scout.sdk.core.s.environment.IEnvironment
import org.eclipse.scout.sdk.core.s.environment.IProgress
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment
import org.eclipse.scout.sdk.s2i.environment.IdeaProgress
import org.eclipse.scout.sdk.s2i.environment.model.JavaEnvironmentWithIdea

fun IType.resolvePsi(): PsiClass? {
    val module = this.javaEnvironment().toIdea().module
    return IdeaEnvironment.computeInReadAction(module.project) { module.project.findTypesByName(name(), GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module, true)).firstOrNull() }
}

fun ProgressIndicator.toScoutProgress(): IdeaProgress = IdeaProgress(this)

fun PsiElement.resolveSourceRoot(): VirtualFile? {
    return this.containingFile
            ?.virtualFile
            ?.let { ProjectFileIndex.getInstance(this.project).getSourceRootForFile(it) }
}

fun PsiElement.containingModule(): Module? {
    return IdeaEnvironment.computeInReadAction(this.project) {
        this
                .takeIf { it.isValid }
                ?.let { ModuleUtil.findModuleForPsiElement(it) }
    }
}

fun IProgress.toIdea(): IdeaProgress = this as IdeaProgress

fun Module.isJavaModule(): Boolean = ModuleRootManager.getInstance(this).sdk?.sdkType == JavaSdk.getInstance()

fun IEnvironment.toIdea(): IdeaEnvironment = this as IdeaEnvironment

fun IJavaEnvironment.toIdea(): JavaEnvironmentWithIdea = this.unwrap() as JavaEnvironmentWithIdea

fun Project.findAllTypesAnnotatedWith(annotation: String, scope: SearchScope): List<PsiClass> = findAllTypesAnnotatedWith(annotation, scope, null)

fun PsiClass.newSubTypeHierarchy(scope: SearchScope, checkDeep: Boolean, includeAnonymous: Boolean, includeRoot: Boolean): Query<PsiClass> {
    val children = ClassInheritorsSearch.search(this, scope, checkDeep, true, includeAnonymous)
    if(!includeRoot) {
        return children
    }

    val resultWithRoot = children.findAll()
    resultWithRoot.add(this)
    return CollectionQuery(resultWithRoot)
}

fun Project.findAllTypesAnnotatedWith(annotation: String, scope: SearchScope, indicator: ProgressIndicator?): List<PsiClass> {
    val options = MatchOptions()
    options.fileType = StdFileTypes.JAVA
    options.isCaseSensitiveMatch = true
    options.isRecursiveSearch = false
    options.scope = scope
    options.searchPattern = "@$annotation( )\nclass \$Class\$ {}"

    val constraint = MatchVariableConstraint()
    constraint.name = "Class"
    options.addVariableConstraint(constraint)

    val result = object : CollectingMatchResultSink() {
        override fun getProgressIndicator(): ProgressIndicator {
            return indicator ?: EmptyProgressIndicator()
        }
    }
    Matcher(this).findMatches(result, options)

    return result.matches
            .asSequence()
            .map { it.match }
            .filter { it.isValid }
            .filter { it.isPhysical }
            .filter { it is PsiClass }
            .map { it as PsiClass }
            .toList()
}

fun Project.findTypesByName(fqn: String) = findTypesByName(fqn, GlobalSearchScope.projectScope(this))

fun Project.findTypesByName(fqn: String, scope: GlobalSearchScope) =
        IdeaEnvironment.computeInReadAction(this) {
            JavaPsiFacade.getInstance(this)
                    .findClasses(fqn, scope)
                    .toSet()
        }
