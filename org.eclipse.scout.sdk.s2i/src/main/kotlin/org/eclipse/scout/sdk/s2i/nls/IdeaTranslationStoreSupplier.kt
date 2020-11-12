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
package org.eclipse.scout.sdk.s2i.nls

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiModifier
import org.eclipse.scout.sdk.core.apidef.IClassNameSupplier
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment
import org.eclipse.scout.sdk.core.model.api.IType
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi
import org.eclipse.scout.sdk.core.s.environment.IEnvironment
import org.eclipse.scout.sdk.core.s.environment.IProgress
import org.eclipse.scout.sdk.core.s.nls.ITranslationStore
import org.eclipse.scout.sdk.core.s.nls.ITranslationStoreSupplier
import org.eclipse.scout.sdk.core.s.nls.TranslationStores
import org.eclipse.scout.sdk.core.s.nls.properties.*
import org.eclipse.scout.sdk.core.s.nls.properties.AbstractTranslationPropertiesFile.parseLanguageFromFileName
import org.eclipse.scout.sdk.s2i.*
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.computeInReadAction
import org.eclipse.scout.sdk.s2i.environment.IdeaProgress
import org.eclipse.scout.sdk.s2i.util.getNioPath
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
        TranslationStores.registerStoreSupplier(this)
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

    private fun resolveSubClasses(module: Module, env: IdeaEnvironment, nameFunction: (IScoutApi) -> IClassNameSupplier): Sequence<TypeMapping> {
        val javaEnv = env.toScoutJavaEnvironment(module) ?: return emptySequence()
        return javaEnv.api(IScoutApi::class.java)
                .map { nameFunction.invoke(it).fqn() }
                .map { resolveSubClasses(module, it, javaEnv) }
                .orElseGet { emptySequence() }
    }

    private fun resolveSubClasses(module: Module, fqn: String, javaEnv: IJavaEnvironment): Sequence<TypeMapping> = computeInReadAction(module.project) {
        val moduleScope = module.getModuleWithDependenciesAndLibrariesScope(false)
        return@computeInReadAction module.project.findTypesByName(fqn, moduleScope)
                .flatMap { it.newSubTypeHierarchy(moduleScope, checkDeep = true, includeAnonymous = false, includeRoot = false).asSequence() }
                .filter { !it.isEnum }
                .filter { it.hasModifierProperty(PsiModifier.PUBLIC) }
                .filter { !it.hasModifierProperty(PsiModifier.ABSTRACT) }
                .filter { it.canNavigateToSource() }
                .map { TypeMapping(it.toScoutType(javaEnv), it) }
                .filter { it.scoutType != null }
    }

    protected fun createTranslationStore(textService: IType, psiClass: PsiClass, progress: IProgress): Optional<ITranslationStore> {
        return PropertiesTextProviderService.create(textService)
                .map { svc -> PropertiesTranslationStore(svc) }
                .filter { store -> loadTranslationFiles(psiClass, store, progress) }
                .map { store -> store }
    }

    protected fun loadTranslationFiles(psiClass: PsiClass, store: PropertiesTranslationStore, progress: IProgress): Boolean {
        val isWritable = psiClass.isWritable
        val rootType = if (isWritable) OrderRootType.SOURCES else OrderRootType.CLASSES
        val roots = findRootDirectories(psiClass, rootType) ?: return false
        val prefix = store.service().filePrefix()
        val folder = store.service().folder()
        val translationFiles = roots
                .asSequence()
                .mapNotNull { it.findFileByRelativePath(folder) }
                .filter { it.isDirectory }
                .filter { it.isValid }
                .flatMap { it.children.asSequence() }
                .filter { !it.isDirectory }
                .mapNotNull { toTranslationPropertiesFile(it, prefix, isWritable) }
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
     * @param rootType The root type. One of the OrderRootType constants. E.g. OrderRootType.SOURCES or OrderRootType.CLASSES
     * @return the source or class roots of the module containing the PsiClass specified
     */
    protected fun findRootDirectories(psiClass: PsiClass, rootType: OrderRootType): Array<VirtualFile>? {
        val module = psiClass.containingModule() ?: return null
        return ModuleRootManager.getInstance(module)
                .fileIndex
                .getOrderEntryForFile(psiClass.containingFile.virtualFile)
                ?.getFiles(rootType)
    }

    protected fun toTranslationPropertiesFile(file: VirtualFile, prefix: String, isEditable: Boolean): ITranslationPropertiesFile? {
        val language = parseLanguageFromFileName(file.name, prefix).orElse(null) ?: return null
        if (isEditable) {
            return EditableTranslationFile(file.getNioPath(), language)
        }
        return ReadOnlyTranslationFile({ file.inputStream }, language, file)
    }

    private data class TypeMapping(val scoutType: IType?, val psiClass: PsiClass) {
        init {
            if (scoutType == null) {
                // warn if a class cannot be found. This may happen if e.g. the packages are wrong configured.
                val fqn = computeInReadAction(psiClass.project) { psiClass.qualifiedName }
                SdkLog.warning("Unable to resolve class '{}'.", fqn)
            }
        }
    }
}