/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i.nls

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiModifier
import org.eclipse.scout.sdk.core.apidef.ITypeNameSupplier
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment
import org.eclipse.scout.sdk.core.model.api.IType
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi
import org.eclipse.scout.sdk.core.s.environment.IEnvironment
import org.eclipse.scout.sdk.core.s.environment.IProgress
import org.eclipse.scout.sdk.core.s.nls.ITranslationStore
import org.eclipse.scout.sdk.core.s.nls.ITranslationStoreSupplier
import org.eclipse.scout.sdk.core.s.nls.Translations
import org.eclipse.scout.sdk.core.s.nls.properties.*
import org.eclipse.scout.sdk.core.s.nls.properties.AbstractTranslationPropertiesFile.parseLanguageFromFileName
import org.eclipse.scout.sdk.s2i.*
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.computeInReadAction
import org.eclipse.scout.sdk.s2i.environment.IdeaProgress
import java.nio.file.Path
import java.util.*
import java.util.stream.Stream
import kotlin.streams.asStream

open class IdeaTranslationStoreSupplier : ITranslationStoreSupplier, StartupActivity, DumbAware {

    override fun visibleStoresForJavaModule(modulePath: Path, env: IEnvironment, progress: IProgress): Stream<ITranslationStore> = modulePath
        .toVirtualFile()
        ?.containingModule(env.toIdea().project)
        ?.let { findJavaTranslationStoresVisibleIn(it, env.toIdea(), progress.toIdea()) }
        ?: Stream.empty()

    override fun createStoreForService(textService: IType, progress: IProgress): Optional<ITranslationStore> {
        val psi = textService.resolvePsi() ?: return Optional.empty()
        progress.init(1, message("load.text.service"))
        return createTranslationStore(textService, psi, progress.newChild(1))
    }

    /**
     * Executed on [Project] open
     */
    override fun runActivity(project: Project) {
        Translations.registerStoreSupplier(this)
    }

    protected fun findJavaTranslationStoresVisibleIn(module: Module, env: IdeaEnvironment, progress: IdeaProgress): Stream<ITranslationStore> {
        progress.init(20, message("search.text.services"))

        val types = resolveSubClasses(module, env, IScoutApi::AbstractDynamicNlsTextProviderService)
        val progressForLoad = progress.worked(10).newChild(10).init(10, message("load.properties.content"))
        val result = types.mapNotNull { createTranslationStore(it.scoutType!!, it.psiClass, progressForLoad).orElse(null) }

        SdkLog.debug("Lookup translation stores on Java classpath of module '{}'.", module.name)
        return result.asStream()
    }

    override fun visibleTextContributorsForJavaModule(modulePath: Path, env: IEnvironment, progress: IProgress): Stream<IType> = modulePath
        .toVirtualFile()
        ?.containingModule(env.toIdea().project)
        ?.let {
            resolveSubClasses(it, env.toIdea(), IScoutApi::IUiTextContributor)
                .mapNotNull { mapping -> mapping.scoutType }
        }
        ?.asStream()
        ?: Stream.empty()

    private fun resolveSubClasses(module: Module, env: IdeaEnvironment, nameFunction: (IScoutApi) -> ITypeNameSupplier): Sequence<TypeMapping> {
        val javaEnv = env.toScoutJavaEnvironment(module) ?: return emptySequence()
        return javaEnv.api(IScoutApi::class.java)
            .map { nameFunction(it).fqn() }
            .map { resolveSubClasses(module, it, javaEnv) }
            .orElseGet { emptySequence() }
    }

    private fun resolveSubClasses(module: Module, fqn: String, javaEnv: IJavaEnvironment): Sequence<TypeMapping> = computeInReadAction(module.project) {
        val moduleScope = module.getModuleWithDependenciesAndLibrariesScope(false)
        val project = module.project
        project.findTypesByName(fqn, moduleScope)
            .flatMap { it.newSubTypeHierarchy(moduleScope, checkDeep = true, includeAnonymous = false, includeRoot = false).asSequence() }
            .filter { !it.isEnum }
            .filter { it.hasModifierProperty(PsiModifier.PUBLIC) }
            .filter { !it.hasModifierProperty(PsiModifier.ABSTRACT) }
            .filter { it.canNavigateToSource() }
            .map { TypeMapping(it.toScoutType(javaEnv), it, project) }
            .filter { it.scoutType != null }
    }

    protected fun createTranslationStore(textService: IType, psiClass: PsiClass, progress: IProgress): Optional<ITranslationStore> {
        return PropertiesTextProviderService.create(textService)
            .map { svc -> PropertiesTranslationStore(svc) }
            .filter { store -> loadTranslationFiles(psiClass, store, progress) }
            .map { store -> store }
    }

    protected fun loadTranslationFiles(psiClass: PsiClass, store: PropertiesTranslationStore, progress: IProgress): Boolean {
        val roots = findRootDirectories(psiClass) ?: return false
        val prefix = store.service().filePrefix()
        val folder = store.service().folder()
        val translationFiles = roots
            .mapNotNull { it.findFileByRelativePath(folder) }
            .filter { it.isValid }
            .filter { it.isDirectory }
            .flatMap { it.children.asSequence() }
            .filter { it.isValid }
            .filter { !it.isDirectory }
            .mapNotNull { toTranslationPropertiesFile(it, prefix) }
            .toList()
        if (translationFiles.isEmpty()) {
            SdkLog.warning("Skipping TextProviderService '{}' because no properties files could be found.", store.service().type().name())
            return false
        }
        store.load(translationFiles, progress)
        return true
    }

    /**
     * @param psiClass The [PsiClass] for which the roots should be returned (the TextProviderService)
     * @return the source or class roots of the module containing the PsiClass specified
     */
    protected fun findRootDirectories(psiClass: PsiClass): Sequence<VirtualFile>? {
        val module = psiClass.containingModule() ?: return null
        val orderEnumerator = module.rootManager
            .orderEntries()
            .withoutSdk()
        val sources = orderEnumerator.allSourceRoots.filter { !it.name.endsWith(".jar") }
        val binaries = orderEnumerator.allLibrariesAndSdkClassesRoots

        val sourcesAndLibraries = sources.toMutableList()
        sourcesAndLibraries.addAll(binaries)
        return sourcesAndLibraries.asSequence()
    }

    protected fun toTranslationPropertiesFile(file: VirtualFile, prefix: String): ITranslationPropertiesFile? {
        val language = parseLanguageFromFileName(file.name, prefix).orElse(null) ?: return null
        val path = file.resolveLocalPath()
        if (path != null) {
            return EditableTranslationFile(path, language)
        }
        return ReadOnlyTranslationFile({ file.inputStream }, language, file)
    }

    private data class TypeMapping(val scoutType: IType?, val psiClass: PsiClass, val project: Project) {
        init {
            if (scoutType == null) {
                // warn if a class cannot be found. This may happen if e.g. the packages are wrong configured.
                val fqn = computeInReadAction(project) { psiClass.qualifiedName }
                SdkLog.warning("Unable to resolve class '{}'.", fqn)
            }
        }
    }
}