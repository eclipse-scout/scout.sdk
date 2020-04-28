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
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiModifier
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment
import org.eclipse.scout.sdk.core.model.api.IType
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes
import org.eclipse.scout.sdk.core.s.environment.IEnvironment
import org.eclipse.scout.sdk.core.s.environment.IProgress
import org.eclipse.scout.sdk.core.s.nls.ITranslationStore
import org.eclipse.scout.sdk.core.s.nls.ITranslationStoreSupplier
import org.eclipse.scout.sdk.core.s.nls.TranslationStores
import org.eclipse.scout.sdk.core.s.nls.properties.*
import org.eclipse.scout.sdk.core.s.nls.properties.AbstractTranslationPropertiesFile.parseFromFileNameOrThrow
import org.eclipse.scout.sdk.core.s.nls.properties.PropertiesTextProviderService.resourceMatchesPrefix
import org.eclipse.scout.sdk.s2i.*
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment
import org.eclipse.scout.sdk.s2i.environment.IdeaProgress
import java.nio.file.Path
import java.util.*
import java.util.function.Supplier
import java.util.stream.Stream

open class IdeaTranslationStoreSupplier : ITranslationStoreSupplier, StartupActivity, DumbAware {

    override fun all(modulePath: Path, env: IEnvironment, progress: IProgress): Stream<ITranslationStore> =
            modulePath.toVirtualFile()
                    ?.containingModule(env.toIdea().project)
                    ?.let { findTranslationStoresVisibleIn(it, env.toIdea(), progress.toIdea()) }
                    ?: Stream.empty()

    override fun single(textService: IType, progress: IProgress): Optional<ITranslationStore> {
        val psi = textService.resolvePsi() ?: return Optional.empty()
        val module = psi.containingModule() ?: return Optional.empty()
        progress.init(1, "Load text provider service")
        return createTranslationStore(textService, psi, module, progress.newChild(1))
    }

    /**
     * Executed on [Project] open
     */
    override fun runActivity(project: Project) {
        TranslationStores.registerStoreSupplier(this)
    }

    protected fun findTranslationStoresVisibleIn(module: Module, env: IdeaEnvironment, progress: IdeaProgress): Stream<ITranslationStore> {
        progress.init(20, "Search properties text provider services.")

        val moduleScope = module.getModuleWithDependenciesAndLibrariesScope(false)
        val javaEnv: IJavaEnvironment = env.toScoutJavaEnvironment(module) ?: return Stream.empty()
        val types = module.project.findTypesByName(IScoutRuntimeTypes.AbstractDynamicNlsTextProviderService, moduleScope)
                .flatMap { it.newSubTypeHierarchy(moduleScope, checkDeep = true, includeAnonymous = false, includeRoot = false) }
                .asSequence()
                .filter { IdeaEnvironment.computeInReadAction(module.project) { !it.isDeprecated } }
                .filter { !it.isEnum }
                .filter { it.hasModifierProperty(PsiModifier.PUBLIC) }
                .filter { !it.hasModifierProperty(PsiModifier.ABSTRACT) }
                .filter { it.scope is PsiJavaFile }
                .filter { it.canNavigateToSource() }
                .map { TypeMapping(it.toScoutType(javaEnv), it) }
                .filter { it.scoutType != null }
                .toList()

        val progressForLoad = progress.worked(10).newChild(10).init(types.size, "Load properties file contents")
        val result = types.mapNotNull { createTranslationStore(it.scoutType!!, it.psiClass, module, progressForLoad).orElse(null) }

        SdkLog.debug("Found translation stores on Java classpath of module '{}': {}", module.name, result)
        return result.stream()
    }

    protected fun createTranslationStore(textService: IType, psiClass: PsiClass, module: Module, progress: IProgress): Optional<ITranslationStore> {
        return PropertiesTextProviderService.create(textService)
                .map { svc -> PropertiesTranslationStore(svc) }
                .filter { store -> loadTranslationFiles(module, psiClass, store, progress) }
                .map { store -> store }
    }

    protected fun loadTranslationFiles(module: Module, psiClass: PsiClass, store: PropertiesTranslationStore, progress: IProgress): Boolean {
        val rootType = if (psiClass.isWritable) OrderRootType.SOURCES else OrderRootType.CLASSES
        val root = findRootDirectories(module, psiClass, rootType) ?: return false

        val prefix = store.service().filePrefix()
        val folder = store.service().folder()
        val translationFiles = root
                .mapNotNull { it.findFileByRelativePath(folder) }
                .filter { it.isDirectory }
                .flatMap { it.children.asIterable() }
                .filter { !it.isDirectory }
                .filter { resourceMatchesPrefix(it.name, prefix) }
                .map { toTranslationPropertiesFile(it, psiClass.isWritable) }
        store.load(translationFiles, progress)
        return true
    }

    /**
     * Gets the source or class roots for the PsiClass specified within the module specified
     * @param module The [Module] in which the root directory should be searched (the Maven module)
     * @param psiClass The [PsiClass] for which the root should be returned (the TextProviderService)
     * @param rootType The root type. One of the OrderRootType constants. E.g. OrderRootType.SOURCES or OrderRootType.CLASSES
     */
    protected fun findRootDirectories(module: Module, psiClass: PsiClass, rootType: OrderRootType): List<VirtualFile>? {
        return ModuleRootManager.getInstance(module)
                .fileIndex
                .getOrderEntryForFile(psiClass.containingFile.virtualFile)
                ?.getFiles(rootType)
                ?.asList()
    }

    protected fun toTranslationPropertiesFile(file: VirtualFile, isEditable: Boolean): ITranslationPropertiesFile {
        if (isEditable) {
            return EditableTranslationFile(file.toNioPath())
        }
        return ReadOnlyTranslationFile(Supplier { file.inputStream }, parseFromFileNameOrThrow(file.name))
    }

    private data class TypeMapping(val scoutType: IType?, val psiClass: PsiClass)
}