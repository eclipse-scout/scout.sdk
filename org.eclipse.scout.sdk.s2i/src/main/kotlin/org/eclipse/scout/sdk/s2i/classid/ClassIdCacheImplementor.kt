/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i.classid

import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.FileContentUtilCore
import com.intellij.util.concurrency.AppExecutorUtil
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi
import org.eclipse.scout.sdk.core.s.apidef.ScoutApi
import org.eclipse.scout.sdk.core.s.dto.AbstractDtoGenerator
import org.eclipse.scout.sdk.core.s.util.DelayedBuffer
import org.eclipse.scout.sdk.s2i.containingModule
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.computeInReadAction
import org.eclipse.scout.sdk.s2i.findAllTypesAnnotatedWith
import org.eclipse.scout.sdk.s2i.util.ApiHelper
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.streams.asSequence

class ClassIdCacheImplementor(val project: Project) : ClassIdCache {

    private val m_fileCache = ConcurrentHashMap<String /* file path */, MutableMap<String /* fqn */, String /* classid */>>()
    private val m_stopTypes: Array<Class<out PsiElement>> = arrayOf(PsiClass::class.java, PsiModifierList::class.java, PsiTypeElement::class.java, PsiTypeParameter::class.java, PsiJavaFile::class.java)
    private val m_delayedProcessor = DelayedBuffer(2, TimeUnit.SECONDS, AppExecutorUtil.getAppScheduledExecutorService(), true, this::processFileEvents)

    @Volatile
    private var m_cacheReady = false

    override fun isCacheReady() = m_cacheReady

    override fun setup() = synchronized(m_fileCache) {
        // synchronized so that only one is creating the cache at a time
        if (isCacheReady()) {
            return
        }

        try {
            computeInReadAction(project) {
                trySetupCache()
            }

            // initial cache is ready, register listeners to keep it up to date
            PsiManager.getInstance(project).addPsiTreeChangeListener(PsiListener(), this)
            VirtualFileManager.getInstance().addAsyncFileListener(VfsListener(), this)

            m_cacheReady = true

            duplicates().forEach { SdkLog.debug("Duplicate @ClassId value '{}' found for types {}.", it.key, it.value) }
        } catch (t: Exception) {
            SdkLog.warning("Error building @ClassId value cache.", t)
        }
    }

    override fun dispose() {
        m_fileCache.clear()
    }

    override fun findAllClassIds(scope: SearchScope, indicator: ProgressIndicator?) =
            ScoutApi.allKnown().asSequence()
                    .map { it.ClassId().fqn() to it }
                    .distinctBy { it.first }
                    .flatMap { findAllClassIds(it.second, scope, indicator) }

    private fun findAllClassIds(scoutApi: IScoutApi, scope: SearchScope, indicator: ProgressIndicator?) =
            project.findAllTypesAnnotatedWith(scoutApi.ClassId().fqn(), scope, indicator)
                    .filter { it.isValid }
                    .mapNotNull { ClassIdAnnotation.of(null, it, scoutApi) }
                    .filter { it.hasValue() }

    override fun typesWithClassId(classId: String): List<String> = usageByClassId()[classId] ?: emptyList()

    override fun duplicates(): Map<String, List<String>> = duplicates(null)

    override fun duplicates(absoluteFilePath: String): Map<String, List<String>> {
        val classIdsInFile = m_fileCache[absoluteFilePath]?.map { it.value }?.toSet() ?: return emptyMap()
        return duplicates { classIdsInFile.contains(it.value) } // only duplicates of the current file
    }

    internal fun duplicates(filter: ((Map.Entry<String, String>) -> Boolean)?) = usageByClassId(filter).filter { it.value.size > 1 }

    internal fun usageByClassId(filter: ((Map.Entry<String, String>) -> Boolean)? = null): Map<String /* classId */, List<String /* fqn */>> {
        val nullSafeFilter = filter ?: { true }
        return m_fileCache.values
                .asSequence()
                .map { it.entries }
                .flatten()
                .filter { nullSafeFilter.invoke(it) }
                .groupBy({ it.value }, { it.key })
    }

    internal fun trySetupCache() {
        SdkLog.debug("Start building @ClassId value cache.")
        val start = System.currentTimeMillis()
        findAllClassIds(GlobalSearchScope.projectScope(project))
                .filter { !ignoreClassId(it) }
                .forEach {
                    val fqn = it.ownerFqn() ?: return@forEach
                    val classIdValue = it.value() ?: return@forEach
                    val filePath = it.psiClass.containingFile.virtualFile.path
                    m_fileCache.computeIfAbsent(filePath) { ConcurrentHashMap() }[fqn] = classIdValue
                }
        SdkLog.debug("Finished building initial @ClassId value cache in {}ms. @ClassId values found in {} files.", System.currentTimeMillis() - start, m_fileCache.size)
    }

    internal fun ignoreClassId(classId: ClassIdAnnotation) = classId.value()?.endsWith(AbstractDtoGenerator.FORMDATA_CLASSID_SUFFIX) ?: true

    internal fun fileCache() = m_fileCache // for testing

    internal fun processFileEvents(events: List<PsiFile>) = events
            .toHashSet()
            .groupBy { it.containingModule() }
            .forEach {
                it.key?.let { module -> processModule(module, it.value) }
            }

    internal fun processModule(module: Module, files: List<PsiFile>) {
        val scoutApi = ApiHelper.scoutApiFor(module) ?: return
        files.forEach {
            computeInReadAction(module.project) {
                processFile(scoutApi, it)
            }
        }
    }

    internal fun processFile(scoutApi: IScoutApi, file: PsiFile) {
        val path = file.virtualFile.path
        val mappingsInFile = HashMap<String /* fqn */, String /* classid value */>()
        file.accept(object : JavaRecursiveElementWalkingVisitor() {
            override fun visitLiteralExpression(expression: PsiLiteralExpression) {
                if (expression.parent !is PsiNameValuePair) {
                    return
                }
                val declaringAnnotation = PsiTreeUtil.getParentOfType(expression, PsiAnnotation::class.java, false, *m_stopTypes) ?: return
                val classId = ClassIdAnnotation.of(declaringAnnotation, scoutApi) ?: return
                if (ignoreClassId(classId)) {
                    return
                }
                val value = classId.value() ?: return
                val qualifiedName = classId.ownerFqn() ?: return
                mappingsInFile[qualifiedName] = value
            }

            override fun visitMethod(method: PsiMethod?) {
                // do not step into methods
            }
        })

        if (mappingsInFile.isEmpty()) {
            m_fileCache.remove(path)
        } else {
            m_fileCache[path] = mappingsInFile
        }
    }

    private inner class PsiListener : PsiTreeChangeAdapter() {
        override fun childrenChanged(event: PsiTreeChangeEvent) {
            val file = event.file ?: return
            if (!file.isPhysical || !file.language.isKindOf(JavaLanguage.INSTANCE)) {
                return
            }
            m_delayedProcessor.submit(file)
        }
    }

    private inner class VfsListener : AsyncFileListener {
        override fun prepareChange(events: MutableList<out VFileEvent>): AsyncFileListener.ChangeApplier? {
            val removedFiles = events.mapNotNull { toFileIfRemoved(it) }
            if (removedFiles.isEmpty()) {
                return null
            }
            return object : AsyncFileListener.ChangeApplier {
                override fun afterVfsChange() {
                    removedFiles.forEach { m_fileCache.remove(it) }
                }
            }
        }

        private fun toFileIfRemoved(event: VFileEvent): String? {
            if (event is VFileDeleteEvent) {
                return event.file.path // deleted
            }
            if (event is VFileMoveEvent) {
                return event.file.path // moved (e.g. refactor)
            }
            if (event is VFilePropertyChangeEvent && event.propertyName == VirtualFile.PROP_NAME && event.requestor != FileContentUtilCore.FORCE_RELOAD_REQUESTOR) {
                return event.oldPath // renamed
            }
            return null
        }
    }
}

